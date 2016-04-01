package synthesis;

/**
 * EmptyTrace
 * <br>
 * Created by Simone Calciolari on 01/04/16.
 *
 * @author Simone Calciolari.
 */
public class SynthEmptyTrace implements SynthTransitionLabel {

	@Override
	public boolean equals(Object o){
		if (o == null){
			return false;
		} else {
			return this.getClass().equals(o.getClass());
		}
	}

	@Override
	public String toString(){
		return "EmptyTrace";
	}
}
