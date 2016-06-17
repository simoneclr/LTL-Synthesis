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

	/**
	 * This method returns the action that the system should perform for the first turn.
	 * If the initial state is also terminal, returns a SUCCESS output, meaning that the game is already won.
	 * <b>NOTE:</b> this method <strong>MUST</strong> be called <strong>EXACTLY ONCE</strong>
	 * at the beginning of every new game, and only at the beginning.
	 * @return A StrategyOutput containing tha first move the system should perform, or SUCCESS if the game is won.
	 */
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

	/**
	 * Used at the end of a turn to proceed to the next state of the game, and returns the action the system
	 * should perform at the next turn.
	 * I.E. if the turn succession is (X_0, Y_0)...(X_n, Y_n), where X_i are the environment moves and Y_i the system ones,
	 * this method takes X_i as input and returns Y_(i+1) as output.
	 * <b>NOTE:</b> when starting a new game, method getFirstMove() <strong>MUST</strong>
	 * be called <strong>EXACTLY ONCE</strong> before calling this method.
	 * @param environmentInput the action performed by the environment during the current turn.
	 * @return a StrategyOutput representing the action the system should perform the next turn,
	 * or SUCCESS if the game is won.
	 */
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
	 * Resets the execution of the game to the initial state.
	 */
	public void resetExecution(){
		this.currentState = (State) this.automaton.initials().iterator().next();
		this.lastStrategyOutput = null;
	}

	/**
	 * Returns the strategy automaton, that represents all the possible strategies for the current problem.
	 * @return an Automaton representing all the possible strategies for the problem at hand.
	 */
	public Automaton getAutomaton(){
		return automaton;
	}
}
