package synthesis;

import formula.ltlf.LTLfLocalVar;
import rationals.Automaton;
import rationals.State;
import synthesis.maps.OutputFunction;
import synthesis.symbols.PartitionedDomain;
import synthesis.symbols.PartitionedWorldLabel;
import synthesis.symbols.PropositionSet;
import synthesis.symbols.SynthTraceInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * SynthesisStrategyGenerator
 * <br>
 * Created by Simone Calciolari on 15/04/16.
 * @author Simone Calciolari.
 */
public class StrategyGenerator {

	private Automaton automaton;
	private PartitionedDomain domain;
	private OutputFunction outputFunction;
	private State currentState;

	public StrategyGenerator(Automaton automaton, PartitionedDomain domain, OutputFunction outputFunction){
		this.automaton = automaton;
		this.domain = domain;
		this.outputFunction = outputFunction;
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

			systemMove = this.outputFunction.get(this.currentState).iterator().next();

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

	public Automaton getAutomaton() {
		return automaton;
	}
}
