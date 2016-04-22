package synthesis;

import rationals.Automaton;
import rationals.State;

import java.util.HashMap;
import java.util.HashSet;

/**
 * SynthesisStrategyGenerator
 * <br>
 * Created by Simone Calciolari on 15/04/16.
 * @author Simone Calciolari.
 */
public class StrategyGenerator {

	private Automaton automaton;
	private HashMap<State, HashSet<PropositionSet>> outputFunction;

	public StrategyGenerator(Automaton automaton, HashMap<State, HashSet<PropositionSet>> outputFunction){
		this.automaton = automaton;
		this.outputFunction = outputFunction;

		System.out.println(this.outputFunction);
	}

	public Automaton getAutomaton() {
		return automaton;
	}
}
