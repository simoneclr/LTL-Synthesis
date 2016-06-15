package synthesis.symbols;

import formula.ltlf.LTLfLocalVar;

import java.util.HashSet;

/**
 * PropositionalWorld
 * <br>
 * Created by Simone Calciolari on 07/04/16.
 *
 * @author Simone Calciolari.
 */
public class PropositionSet extends HashSet<LTLfLocalVar> implements SynthTraceInput, StrategyOutput {
	@Override
	public String toString(){
		return super.toString();
	}
}
