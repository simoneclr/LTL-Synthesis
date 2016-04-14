package synthesis;

import formula.ltlf.LTLfFormula;
import rationals.Automaton;
import rationals.State;
import rationals.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

	private State currentState;

	public SynthesisAutomaton(PartitionedDomain domain, LTLfFormula formula){
		this.domain = domain;
		Automaton tmp = buildLTLfAutomaton(formula);
		this.automaton = transalteToGameAutomaton(tmp, domain);

		this.computeTransitionMaps();

		System.out.println(this.transitionMap);
		System.out.println(this.emptyTraceTransitionMap);

		System.out.println(this.computeRealizability());
		System.out.println(this.transducerOutputFunction);

		this.currentState = (State) this.automaton.initials().iterator().next();
	}

	public PropositionSet step(SynthTraceInput environmentInput){
		PropositionSet systemMove = new PropositionSet();

		if (environmentInput instanceof PropositionSet){
			PropositionSet environmentMove = (PropositionSet) environmentInput;
			systemMove = this.transducerOutputFunction.get(this.currentState).iterator().next();

			PartitionedWorldLabel label = new PartitionedWorldLabel(environmentMove, systemMove);
			Set<State> currentStateSet = this.automaton.getStateFactory().stateSet();
			currentStateSet.add(this.currentState);
			Set<State> arrivalStates = this.automaton.step(currentStateSet, label);

			if (arrivalStates.size() != 1){
				throw new RuntimeException("Error! Automaton is not deterministic");
			} else {
				this.currentState = arrivalStates.iterator().next();
			}
		}

		return systemMove;
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
		HashSet<State> newWinningStates = new HashSet<>();
		newWinningStates.addAll(this.automaton.terminals());

		while (!winningStates.equals(newWinningStates)){
			winningStates.addAll(newWinningStates);
			newWinningStates = new HashSet<>();

			//TODO Maybe use non-winning states only?
			for (State s : (Set<State>) this.automaton.states()){
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

		System.out.println("Winning states: " + winningStates);
		return winningStates.contains(this.automaton.initials().iterator().next());
	}
}