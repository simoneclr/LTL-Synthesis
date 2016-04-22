package synthesis;

import formula.ltlf.LTLfFormula;
import formula.ltlf.LTLfLocalVar;
import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

import java.util.*;

import static util.AutomatonUtils.*;

/**
 * SynthesisAutomaton
 * <br>
 * Created by Simone Calciolari on 01/04/16.
 * @author Simone Calciolari.
 */
public class SynthesisAutomaton {

	private Automaton automaton;
	private PartitionedDomain domain;

	private TransitionMap transitionMap;
	private HashMap<State, State> emptyTraceTransitionMap;
	private HashMap<State, HashSet<PropositionSet>> transducerOutputFunction;

	private HashSet<State> winningStates;
	private boolean realizable;

	public SynthesisAutomaton(PartitionedDomain domain, LTLfFormula formula){
		this.domain = domain;
		Automaton tmp = buildLTLfAutomaton(formula);
		this.automaton = transalteToGameAutomaton(tmp, domain);

		this.computeTransitionMaps();

		this.realizable = this.computeRealizability();
	}

	public StrategyGenerator getStrategyGenerator(){
		if (this.isRealizable()){
			Automaton strategyAutomaton = new Automaton();
			HashMap<State, HashSet<PropositionSet>> strategyMap = new HashMap<>();

			//Map to translate states
			HashMap<State, State> oldToNewStates = new HashMap<>();

			//Add states to the new automaton and fill the map
			for (State oldState: this.winningStates){
				State newState = strategyAutomaton.addState(oldState.isInitial(), oldState.isTerminal());
				oldToNewStates.put(oldState, newState);
			}

			for (State oldStart : this.winningStates){
				Set<Transition<SynthTransitionLabel>> oldTransitions = this.automaton.delta(oldStart);
				State newStart = oldToNewStates.get(oldStart);

				//Update the strategy map
				strategyMap.putIfAbsent(newStart, new HashSet<>());
				strategyMap.get(newStart).addAll(this.transducerOutputFunction.get(oldStart));

				for (Transition<SynthTransitionLabel> oldTransition: oldTransitions){
					//If it's a winning transition, add it to the strategy generator
					SynthTransitionLabel oldLabel = oldTransition.label();
					State oldEnd = oldTransition.end();

					if (this.winningStates.contains(oldEnd)){
						SynthTransitionLabel newLabel = null;

						if (oldLabel instanceof SynthEmptyTrace){
							if (this.winningStates.contains(this.emptyTraceTransitionMap.get(oldStart))){
								newLabel = new SynthEmptyTrace();
							}
						} else {
							PartitionedWorldLabel oldPwl = (PartitionedWorldLabel) oldLabel;
							if (this.transducerOutputFunction.get(oldStart).contains(oldPwl.getSystemDomain())){
								newLabel = new PartitionedWorldLabel(oldPwl.getEnvironmentDomain(), oldPwl.getSystemDomain());
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

			return new StrategyGenerator(strategyAutomaton, this.domain, strategyMap);
		} else {
			throw new RuntimeException("Problem is not realizable!");
		}
	}

	private HashSet<State> computeWinningFinalStates(Set<State> states){
		HashSet<State> res = new HashSet<>();

		for (State s : states){
			if (states.contains(this.emptyTraceTransitionMap.get(s))){
				for (PropositionSet y : transitionMap.get(s).keySet()){
					if (states.containsAll(transitionMap.get(s).get(y))){
						res.add(s);
						this.transducerOutputFunction.putIfAbsent(s, new HashSet<>());
						this.transducerOutputFunction.get(s).add(y);
					}
				}
			}
		}

		return res;
	}

	private void computeTransitionMaps(){
		this.transitionMap = new TransitionMap();
		this.emptyTraceTransitionMap = new HashMap<>();

		for (State s : (Set<State>) this.automaton.states()){
			this.transitionMap.putIfAbsent(s, new HashMap<>());

			Set<Transition<SynthTransitionLabel>> transitions = this.automaton.delta(s);

			for (Transition<SynthTransitionLabel> t : transitions){
				SynthTransitionLabel label = t.label();
				State endState = t.end();

				if (label instanceof PartitionedWorldLabel){
					PropositionSet system = ((PartitionedWorldLabel) label).getSystemDomain();
					PropositionSet environment = ((PartitionedWorldLabel) label).getEnvironmentDomain();

					transitionMap.get(s).putIfAbsent(system, new HashSet<>());
					transitionMap.get(s).get(system).add(endState);

				} else if (label instanceof SynthEmptyTrace){
					this.emptyTraceTransitionMap.put(s, endState);
				} else {
					throw new RuntimeException("Unknown label type");
				}
			}
		}
	}

	private boolean computeRealizability(){
		this.transducerOutputFunction = new HashMap<>();

		HashSet<State> winningStates = new HashSet<>();
		HashSet<State> terminals = new HashSet<>();
		terminals.addAll(this.automaton.terminals());
		HashSet<State> newWinningStates = this.computeWinningFinalStates(terminals);

		while (!winningStates.equals(newWinningStates)){
			winningStates.addAll(newWinningStates);
			newWinningStates = new HashSet<>();

			//TODO Maybe use non-winning states only?
			HashSet<State> nonWinningStates = new HashSet<>();
			nonWinningStates.addAll(this.automaton.states());
			nonWinningStates.removeAll(winningStates);

			for (State s : nonWinningStates){
				if (winningStates.contains(this.emptyTraceTransitionMap.get(s))){
					for (PropositionSet y : transitionMap.get(s).keySet()){
						if (winningStates.containsAll(this.transitionMap.get(s).get(y))){
							newWinningStates.add(s);
							this.transducerOutputFunction.putIfAbsent(s, new HashSet<>());
							this.transducerOutputFunction.get(s).add(y);
						}
					}
				}
			}

			newWinningStates.addAll(winningStates);
		}

		this.winningStates = winningStates;
		return winningStates.contains(this.automaton.initials().iterator().next());
	}


	//<editor-fold desc="Getter Methods" defaultState="collapsed">
	public boolean isRealizable() {
		return realizable;
	}

	public HashMap<State, HashSet<PropositionSet>> getTransducerOutputFunction() {
		return transducerOutputFunction;
	}

	public PartitionedDomain getDomain() {
		return domain;
	}
	//</editor-fold>
}