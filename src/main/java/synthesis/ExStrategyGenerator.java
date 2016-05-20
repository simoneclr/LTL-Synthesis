package synthesis;

import formula.ltlf.LTLfLocalVar;
import rationals.Automaton;
import rationals.State;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * ExStrategyGenerator
 * <br>
 * Created by Simone Calciolari on 20/05/16.
 *
 * @author Simone Calciolari.
 */
public class ExStrategyGenerator {

	protected Automaton automaton;
	protected PartitionedDomain domain;
	protected State currentState;

	protected HashMap<State, HashSet<PropositionSet>> safeOutputFunction;
	private HashMap<State, HashSet<PropositionSet>> exOutputFunction;

	public ExStrategyGenerator(Automaton automaton, PartitionedDomain domain,
														 HashMap<State, HashSet<PropositionSet>> safeOutputFunction,
														 HashMap<State, HashSet<PropositionSet>> exOutputFunction){

		this.automaton = automaton;
		this.domain = domain;
		this.safeOutputFunction = safeOutputFunction;
		this.exOutputFunction = exOutputFunction;
		this.currentState = (State) this.automaton.initials().iterator().next();
	}

	public PropositionSet step(SynthTraceInput environmentInput){
		PropositionSet systemMove = new PropositionSet();

		if (environmentInput instanceof PropositionSet){
			PropositionSet environmentMove = (PropositionSet) environmentInput;

			for (LTLfLocalVar v : environmentMove){
				if (this.domain.getSystemDomain().contains(v)){
					throw new RuntimeException("Proposition " + v + " is part of the system domain!");
				} else if (!this.domain.getEnvironmentDomain().contains(v)){
					throw new RuntimeException("Proposition " + v + " is not part of the environment domain");
				}
			}

			if (this.safeOutputFunction.get(this.currentState) != null &&
					!this.safeOutputFunction.get(this.currentState).isEmpty()){
				//A safe move exists
				systemMove = this.safeOutputFunction.get(this.currentState).iterator().next();
			} else {
				//No safe move, pick one of the possibly good moves
				//TODO how do we choose one?
				systemMove = this.exOutputFunction.get(this.currentState).iterator().next();
			}

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

}
