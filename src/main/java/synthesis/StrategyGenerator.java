package synthesis;

import rationals.Automaton;

/**
 * SynthesisStrategyGenerator
 * <br>
 * Created by Simone Calciolari on 15/04/16.
 * @author Simone Calciolari.
 */
public class StrategyGenerator {

	private Automaton automaton;

	public StrategyGenerator(Automaton automaton){
		this.automaton = automaton;
	}

	public Automaton getAutomaton() {
		return automaton;
	}
}
