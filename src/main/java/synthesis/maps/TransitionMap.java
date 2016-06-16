package synthesis.maps;

import rationals.State;
import synthesis.symbols.Interpretation;
import synthesis.symbols.PropositionSet;

import java.util.HashMap;
import java.util.HashSet;

/**
 * TransitionMapBis
 * <br>
 * Created by Simone Calciolari on 14/04/16.
 * @author Simone Calciolari.
 */
public class TransitionMap extends HashMap<State, HashMap<Interpretation, HashSet<State>>> {
}
