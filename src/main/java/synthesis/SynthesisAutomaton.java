package synthesis;

import formula.ltlf.LTLfFormula;
import formula.ltlf.LTLfLocalVar;
import net.sf.tweety.logics.pl.syntax.Proposition;
import net.sf.tweety.logics.pl.syntax.PropositionalSignature;
import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;
import synthesis.maps.OutputFunction;
import synthesis.maps.TransitionMap;
import synthesis.symbols.*;

import java.util.*;

import static util.AutomatonUtils.*;

/**
 * SynthesisAutomaton
 * Class that handles all the information and computation required to solve the realizability-synthesis
 * problem for a given LTL formula, given the partition of domain between propositions controlled by the
 * environment and by the system
 * <br>
 * Created by Simone Calciolari on 01/04/16.
 * @author Simone Calciolari.
 *
 * LTL-Synthesis. Perform LTL Synthesis on finite traces. Copyright (C) 2016 Simone Calciolari
 *
 * This file is part of LTL-Synthesis.
 *
 * LTL-Synthesis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LTL-Synthesis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LTL-Synthesis. If not, see <http://www.gnu.org/licenses/>.
 */
public class SynthesisAutomaton {

	private Automaton automaton;
	private PartitionedDomain domain;

	private TransitionMap transitionMap;
	//private HashMap<State, State> emptyTraceTransitionMap;
	private OutputFunction outputFunction;

	private HashSet<State> winningStates;
	private boolean realizable;

	/**
	 * Instantiates a new SynthesisAutomaton.
	 * @param domain the domain of the problem, partitioned in propositions controlled by the environment and by the system
	 * @param formula the LTL formula that serves as specification for the synthesis problem
	 */
	public SynthesisAutomaton(PartitionedDomain domain, LTLfFormula formula){
		this.domain = domain;

		PropositionalSignature ps = formula.getSignature();
		for (Proposition p : ps){
			LTLfLocalVar lv = new LTLfLocalVar(p);
			if (!this.domain.getCompleteDomain().contains(lv)){
				throw new RuntimeException("Unkown proposition " + lv);
			}
		}

		Automaton tmp = buildLTLfAutomaton(formula);
		this.automaton = transalteToGameAutomaton(tmp, domain);

		this.computeTransitionMaps();

		this.realizable = this.computeRealizability();
	}

	/**
	 * Returns the solutions (if they exist) for the current problem
	 * @return a StrategyGenerator that carries the solutions to the current problem (if they exist); null otherwise
	 */
	public StrategyGenerator getStrategyGenerator(){
		if (this.isRealizable()){
			Automaton strategyAutomaton = new Automaton();
			OutputFunction strategyMap = new OutputFunction();

			//Map to translate states
			HashMap<State, State> oldToNewStates = new HashMap<>();

			//Add the winning states to the new automaton and fill the map
			for (State oldState: this.winningStates){
				State newState = strategyAutomaton.addState(oldState.isInitial(), oldState.isTerminal());
				oldToNewStates.put(oldState, newState);
			}

			for (State oldStart : this.winningStates){
				Set<Transition<SynthTransitionLabel>> oldTransitions = this.automaton.delta(oldStart);
				State newStart = oldToNewStates.get(oldStart);

				strategyMap.putIfAbsent(newStart, new HashSet<>());

				if (!this.automaton.terminals().contains(oldStart)){
					//No point in adding outgoing transitions from terminal states

					//Update strategy map
					strategyMap.get(newStart).addAll(this.outputFunction.get(oldStart));

					for (Transition<SynthTransitionLabel> oldTransition: oldTransitions){
						SynthTransitionLabel oldLabel = oldTransition.label();
						State oldEnd = oldTransition.end();

						//If it's a winning transition, add it to the strategy generator
						if (this.winningStates.contains(oldEnd)){
							SynthTransitionLabel newLabel = null;

							if (oldLabel instanceof SynthEmptyTrace){
								/*
								if (this.winningStates.contains(this.emptyTraceTransitionMap.get(oldStart))){
									newLabel = new SynthEmptyTrace();
								}*/
							} else {
								PartitionedInterpretation oldPwl = (PartitionedInterpretation) oldLabel;
								if (this.outputFunction.get(oldStart).contains(oldPwl.getSystemInterpretation())){
									newLabel = new PartitionedInterpretation(oldPwl.getEnvironmentInterpretation(),
											oldPwl.getSystemInterpretation());
								}
							}

							//i.e. if it's a winning transition
							if (newLabel != null){
								State newEnd = oldToNewStates.get(oldEnd);
								Transition<SynthTransitionLabel> newTransition = new Transition<>(newStart, newLabel, newEnd);

								try {
									strategyAutomaton.addTransition(newTransition);
								} catch (NoSuchStateException e){
									throw new RuntimeException(e);
								}
							}
						}
					}
				}
			}

			return new StrategyGenerator(strategyAutomaton, this.domain, strategyMap);
		} else {
			return null;
		}
	}

	private boolean computeRealizability(){
		HashSet<State> winningStates = new HashSet<>();
		HashSet<State> terminals = new HashSet<>();
		terminals.addAll(this.automaton.terminals());
		HashSet<State> newWinningStates = terminals;

		this.outputFunction = new OutputFunction();

		while (!winningStates.equals(newWinningStates)){
			winningStates.addAll(newWinningStates);
			newWinningStates = new HashSet<>();

			HashSet<State> nonWinningStates = new HashSet<>();
			nonWinningStates.addAll(this.automaton.states());
			nonWinningStates.removeAll(winningStates);

			for (State s : nonWinningStates){
				for (Interpretation y : transitionMap.get(s).keySet()){
					if (winningStates.containsAll(this.transitionMap.get(s).get(y))){
						newWinningStates.add(s);
						this.outputFunction.putIfAbsent(s, new HashSet<>());
						this.outputFunction.get(s).add(y);
					}
				}
			}

			newWinningStates.addAll(winningStates);
		}

		this.winningStates = winningStates;
		return winningStates.contains(this.automaton.initials().iterator().next());
	}

	private void computeTransitionMaps(){
		this.transitionMap = new TransitionMap();
		//this.emptyTraceTransitionMap = new HashMap<>();

		for (State s : (Set<State>) this.automaton.states()){
			this.transitionMap.putIfAbsent(s, new HashMap<>());

			Set<Transition<SynthTransitionLabel>> transitions = this.automaton.delta(s);

			for (Transition<SynthTransitionLabel> t : transitions){
				SynthTransitionLabel label = t.label();
				State endState = t.end();

				if (label instanceof PartitionedInterpretation){
					Interpretation system = ((PartitionedInterpretation) label).getSystemInterpretation();
					Interpretation environment = ((PartitionedInterpretation) label).getEnvironmentInterpretation();

					transitionMap.get(s).putIfAbsent(system, new HashSet<>());
					transitionMap.get(s).get(system).add(endState);

				} else if (label instanceof SynthEmptyTrace){
					//this.emptyTraceTransitionMap.put(s, endState);
				} else {
					throw new RuntimeException("Unknown label type");
				}
			}
		}
	}

	private OutputFunction initOutputFunction(Set<State> states){
		OutputFunction res = new OutputFunction();

		for (State s : states){
			for (Interpretation y : transitionMap.get(s).keySet()){
				if (states.containsAll(this.transitionMap.get(s).get(y))){
					res.putIfAbsent(s, new HashSet<>());
					res.get(s).add(y);
				}
			}
		}

		return res;
	}


	//<editor-fold desc="Getter Methods" defaultState="collapsed">
	public boolean isRealizable() {
		return realizable;
	}

	public OutputFunction getOutputFunction() {
		return outputFunction;
	}

	public PartitionedDomain getDomain() {
		return domain;
	}
	//</editor-fold>
}