package synthesis.symbols;

import formula.ltlf.LTLfLocalVar;

/**
 * PartitionedInterpretation
 * Class that represents an interpretation partitioned between proposition controlled by the environment and
 * controlled by the system.
 * <br>
 * Created by Simone Calciolari on 01/04/16.
 *
 * @author Simone Calciolari.
 */
public class PartitionedInterpretation implements SynthTransitionLabel {

	private Interpretation environmentInterpretation;
	private Interpretation systemInterpretation;

	/**
	 * Instantiates a new PartitionedDomain
	 * @param environmentInterpretation the interpretation over the propositions controlled by the environment
	 * @param systemInterpretation the interpretation over the propositions controlled by the system
	 */
	public PartitionedInterpretation(Interpretation environmentInterpretation, Interpretation systemInterpretation){
		for (LTLfLocalVar x : environmentInterpretation){
			if (systemInterpretation.contains(x)){
				throw new RuntimeException("System and environment interpretations must be disjoint; " +
						"Proposition " + x + " appears in both.");
			}
		}

		this.environmentInterpretation = environmentInterpretation;
		this.systemInterpretation = systemInterpretation;
	}

	@Override
	public boolean equals(Object o){
		if (o != null){
			if (o instanceof PartitionedInterpretation){
				PartitionedInterpretation other = (PartitionedInterpretation) o;

				return (this.getClass().equals(other.getClass())
						&& this.environmentInterpretation.equals(other.getEnvironmentInterpretation())
						&& this.systemInterpretation.equals(other.getSystemInterpretation()));
			}
		}

		return false;
	}

	@Override
	public String toString(){
		return "Environment: " + this.environmentInterpretation.toString() +
				"; System: " + this.systemInterpretation.toString();
	}

	/**
	 * Retrieves the union of the system and environment partitions
	 * @return an Interpretations containing all the true propositions
	 */
	public Interpretation getCompleteDomain(){
		Interpretation res = new Interpretation();
		res.addAll(this.environmentInterpretation);
		res.addAll(this.systemInterpretation);
		return res;
	}

	/**
	 * Retrieves the interpretation of the propositions controlled by the environment
	 * @return an Interpretation over the propositions controlled by the environment
	 */
	public Interpretation getEnvironmentInterpretation(){
		return environmentInterpretation;
	}

	/**
	 * Retrieves the interpretation of the propositions controlled by the system
	 * @return an Interpretation over the propositions controlled by the system
	 */
	public Interpretation getSystemInterpretation(){
		return systemInterpretation;
	}
}
