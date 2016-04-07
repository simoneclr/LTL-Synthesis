package synthesis;

/**
 * PartitionedWorldLabel
 * <br>
 * Created by Simone Calciolari on 01/04/16.
 *
 * @author Simone Calciolari.
 */
public class PartitionedWorldLabel extends PartitionedDomain implements SynthTransitionLabel {

	public PartitionedWorldLabel(){
		super();
	}

	public PartitionedWorldLabel(PropositionSet environmentDomain, PropositionSet systemDomain){
		super(environmentDomain, systemDomain);
	}

}
