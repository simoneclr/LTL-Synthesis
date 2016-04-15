package synthesis;

import formula.ltlf.LTLfFormula;
import formula.ltlf.LTLfLocalVar;
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
	private boolean realizable;

	public SynthesisAutomaton(PartitionedDomain domain, LTLfFormula formula){
		this.domain = domain;
		Automaton tmp = buildLTLfAutomaton(formula);
		this.automaton = transalteToGameAutomaton(tmp, domain);

		this.computeTransitionMaps();

		this.realizable = this.computeRealizability();

		this.currentState = (State) this.automaton.initials().iterator().next();
	}

	public PropositionSet step(SynthTraceInput environmentInput){
		PropositionSet systemMove = new PropositionSet();

		if (this.isRealizable()){
			if (environmentInput instanceof PropositionSet){
				PropositionSet environmentMove = (PropositionSet) environmentInput;

				for (LTLfLocalVar v : environmentMove){
					if (this.domain.getSystemDomain().contains(v)){
						throw new RuntimeException("Proposition " + v + " is part of the system domain!");
					}
				}

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
		} else {
			throw new RuntimeException("The current problem is not realizable!");
		}

		return systemMove;
	}

	public ArrayList<PropositionSet> batchSteps(ArrayList<SynthTraceInput> environmentMoves){
		ArrayList<PropositionSet> systemMoves = new ArrayList<>();

		for (SynthTraceInput em : environmentMoves){
			systemMoves.add(this.step(em));
		}

		return systemMoves;
	}

	public void resetExecution(){
		this.currentState = (State) this.automaton.initials().iterator().next();
	}

	private HashSet<State> computeWinningFinalStates(Set<State> states){
		HashSet<State> res = new HashSet<>();

		for (State s : states){
			if (states.contains(this.emptyTraceTransitionMap.get(s))){
				for (PropositionSet y : transitionMap.get(s).keySet()){
					if (states.containsAll(transitionMap.get(s).get(y))){
						res.add(s);
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