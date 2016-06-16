package synthesis;

import formula.ltlf.LTLfLocalVar;
import rationals.Automaton;
import rationals.State;
import synthesis.maps.OutputFunction;
import synthesis.symbols.*;

import java.util.ArrayList;
import java.util.Set;

/**
 * SynthesisStrategyGenerator
 * This class contains all possible solutions for a synthesis problem, and allows to explore them
 * <br>
 * Created by Simone Calciolari on 15/04/16.
 * @author Simone Calciolari.
 */
public class StrategyGenerator {

	private Automaton automaton;
	private PartitionedDomain domain;

	private State currentState;
	private StrategyOutput lastStrategyOutput;

	private OutputFunction outputFunction;

	/**
	 * Instatiates a new StrategyGenerator
	 * @param automaton the automaton containing the strategies
	 * @param domain the partitioned domain of the problem
	 * @param outputFunction the output function for the automaton
	 */
	public StrategyGenerator(Automaton automaton, PartitionedDomain domain, OutputFunction outputFunction){
		this.automaton = automaton;
		this.domain = domain;
		this.outputFunction = outputFunction;
		this.currentState = (State) this.automaton.initials().iterator().next();
		this.lastStrategyOutput = null;
	}

	public StrategyOutput getFirstMove(){
		StrategyOutput res;

		if (this.currentState.isInitial()){
			if (this.currentState.isTerminal()){
				res = new StrategySuccessOutput();
			} else {
				//Get first winning move from initial state
				res = this.outputFunction.get(this.currentState).iterator().next();
				this.lastStrategyOutput = res;
			}
		} else {
			throw new RuntimeException("Cannot perform this action in the current state");
		}

		return res;
	}

	public StrategyOutput step(SynthTraceInput environmentInput){
		StrategyOutput res;

		if (this.lastStrategyOutput == null){
			if (this.currentState.isInitial()){
				throw new RuntimeException("Method getFirstMove() must be called first");
			} else {
				throw new RuntimeException("An unpredicted error occurred");
			}
		} else if (this.lastStrategyOutput instanceof StrategySuccessOutput){
			res = new StrategySuccessOutput();
		} else if (this.currentState.isTerminal()){
			res = new StrategySuccessOutput();
		} else {

			if (environmentInput instanceof Interpretation){
				Interpretation environmentMove = (Interpretation) environmentInput;

				//Input sanity checks
				for (LTLfLocalVar v : environmentMove){
					if (this.domain.getSystemDomain().contains(v)){
						throw new RuntimeException("Proposition " + v + " is part of the system domain!");
					} else if (!this.domain.getEnvironmentDomain().contains(v)){
						throw new RuntimeException("Proposition " + v + " is not part of the environment domain");
					}
				}

				Interpretation lastSystemInterpretation;

				if (this.lastStrategyOutput instanceof Interpretation){
					lastSystemInterpretation = (Interpretation) this.lastStrategyOutput;
				} else {
					throw new RuntimeException("An unpredicted error occurred");
				}

				//Use previous selected move + environment move to move to next state
				PartitionedInterpretation label = new PartitionedInterpretation(environmentMove, lastSystemInterpretation);
				Set<State> currentStateSet = this.automaton.getStateFactory().stateSet();
				currentStateSet.add(this.currentState);
				Set<State> arrivalStates = this.automaton.step(currentStateSet, label);

				//Update current state
				if (arrivalStates.size() != 1){
					throw new RuntimeException("Error! Automaton is not deterministic");
				} else {
					this.currentState = arrivalStates.iterator().next();
				}

				if (this.currentState.isTerminal()){
					res = new StrategySuccessOutput();
				} else {
					//Select and return next strategy move from the new current state
					this.lastStrategyOutput = this.outputFunction.get(this.currentState).iterator().next();
					res = this.lastStrategyOutput;
				}

			} else {
				throw new RuntimeException("Invalid environment input");
			}
		}

		return res;
	}

	/**
	 * resets the execution to the initial state of the automaton
	 */
	public void resetExecution(){
		this.currentState = (State) this.automaton.initials().iterator().next();
		this.lastStrategyOutput = null;
	}

	public Automaton getAutomaton(){
		return automaton;
	}
}
