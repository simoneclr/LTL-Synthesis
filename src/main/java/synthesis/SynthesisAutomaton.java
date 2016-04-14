package synthesis;

import formula.ltlf.LTLfFormula;
import rationals.Automaton;
import rationals.State;
import rationals.Transition;

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
	private HashSet<PropositionSet> allSystemWorlds;
	private HashSet<PropositionSet> allEnvironmentWorlds;

	private HashMap<State, HashSet<PropositionSet>> transducerOutputFunction;

	public SynthesisAutomaton(PartitionedDomain domain, LTLfFormula formula){
		this.domain = domain;
		Automaton tmp = buildLTLfAutomaton(formula);
		this.automaton = transalteToGameAutomaton(tmp, domain);

		System.out.println(this.computeRealizability());
		System.out.println(this.transducerOutputFunction);
	}

	private boolean computeRealizability(){
		this.transducerOutputFunction = new HashMap<>();

		HashSet<State> winningStates = new HashSet<>();
		HashSet<State> newWinningStates = new HashSet<>();

		newWinningStates.addAll(this.automaton.terminals());

		while (!winningStates.equals(newWinningStates)){
			//?????
			//winningStates = new HashSet<>();
			winningStates.addAll(newWinningStates);
			newWinningStates = new HashSet<>();
			//newWinningStates.addAll(winningStates);

			//TODO Maybe use non-winning states only?
			for (State s : (Set<State>) this.automaton.states()){
				TransitionMap transitionMap = this.computeTransitionMap(s, winningStates);

				for (PropositionSet y : transitionMap.keySet()){
					HashSet<State> endStates = transitionMap.get(y);

					boolean allWinning = true;

					for (State es: endStates){
						if (!winningStates.contains(es)){
							allWinning = false;
						}
					}

					if (allWinning){
						newWinningStates.add(s);
						this.transducerOutputFunction.putIfAbsent(s, new HashSet<>());
						this.transducerOutputFunction.get(s).add(y);
					}
				}
			}

			newWinningStates.addAll(winningStates);
		}

		System.out.println("Winning states: " + winningStates);
		return winningStates.contains(this.automaton.initials().iterator().next());
	}

	private TransitionMap computeTransitionMap(State s, HashSet<State> winningStates){
		TransitionMap transitionMap = new TransitionMap();
		Set<Transition<SynthTransitionLabel>> transitions = this.automaton.delta(s);

		boolean emptyTraceWin = true;

		for (Transition<SynthTransitionLabel> t : transitions){
			SynthTransitionLabel label = t.label();
			State endState = t.end();

			if (label instanceof PartitionedWorldLabel){
				PropositionSet system = ((PartitionedWorldLabel) label).getSystemDomain();
				transitionMap.putIfAbsent(system, new HashSet<>());
				transitionMap.get(system).add(endState);

			} else if (label instanceof SynthEmptyTrace){
				emptyTraceWin = winningStates.contains(t.end());
			} else {
				throw new RuntimeException("Unknown label type");
			}
		}

		if (!emptyTraceWin) {
			transitionMap = new TransitionMap();
		}

		return transitionMap;
	}

	//<editor-fold desc="Old implementation (Wrong btw)" dafaultState="collapsed">
	/*
	private boolean computeRealizabilityOld(){

		HashSet<State> win = new HashSet<>();
		HashSet<State> newWin = new HashSet<>();

		newWin.addAll(this.automaton.terminals());

		while (!win.equals(newWin)){
			//?????
			win = new HashSet<>();
			win.addAll(newWin);
			newWin = new HashSet<>();
			newWin.addAll(win);

			//TODO Maybe use non-winning states only?
			for (State s : (Set<State>) this.automaton.states()){
				TransitionMapOld transitionMap = this.computeTransitionMapOld(s);

				for (PropositionSet y : transitionMap.keySet()){
					HashMap<State, HashSet<PropositionSet>> statePsSetMap = transitionMap.get(y);

					for (State ends : statePsSetMap.keySet()){
						//This is wrong: it must be that all end states belong to the winning set
						if (win.contains(ends)){
							if (statePsSetMap.get(ends).equals(this.allEnvironmentWorlds)){
								newWin.add(s);
							}
						}
					}
				}
			}
		}

		return win.contains(this.automaton.initials().iterator().next());
	}

	private TransitionMapOld computeTransitionMapOld(State s){
		TransitionMapOld transitionMap = new TransitionMapOld();

		Set<Transition<SynthTransitionLabel>> transitions = this.automaton.delta(s);

		for (Transition<SynthTransitionLabel> t : transitions){
			SynthTransitionLabel label = t.label();
			State endState = t.end();

			if (label instanceof PartitionedWorldLabel){
				PropositionSet system = ((PartitionedWorldLabel) label).getSystemDomain();
				PropositionSet environment = ((PartitionedWorldLabel) label).getEnvironmentDomain();

				transitionMap.putIfAbsent(system, new HashMap<>());
				transitionMap.get(system).putIfAbsent(endState, new HashSet<>());
				transitionMap.get(system).get(endState).add(environment);
			}
		}

		return transitionMap;
	}

	private void computeAllPossibleWorlds(){
		ArrayList<LTLfLocalVar> system = new ArrayList<>();
		ArrayList<LTLfLocalVar> environment = new ArrayList<>();

		system.addAll(this.domain.getSystemDomain());
		environment.addAll(this.domain.getEnvironmentDomain());

		this.allSystemWorlds = this.allSubsets(system, 0);
		this.allEnvironmentWorlds = this.allSubsets(environment, 0);
	}

	private HashSet<PropositionSet> allSubsets(ArrayList<LTLfLocalVar> propositions, int i){
		HashSet<PropositionSet> res = new HashSet<>();

		if (i == propositions.size()){
			res.add(new PropositionSet());
		} else {
			HashSet<PropositionSet> tmp = allSubsets(propositions, i + 1);
			LTLfLocalVar v = propositions.get(i);

			for (PropositionSet ps : tmp){
				PropositionSet newPs = new PropositionSet();
				newPs.addAll(ps);
				newPs.add(v);
				res.add(newPs);
			}

			res.addAll(tmp);
		}

		return res;
	}
	*/
	//</editor-fold>

}