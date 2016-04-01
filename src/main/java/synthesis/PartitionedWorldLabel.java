package synthesis;

import formula.ltlf.LTLfLocalVar;

import java.util.HashSet;

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

	public PartitionedWorldLabel(HashSet<LTLfLocalVar> environmentDomain, HashSet<LTLfLocalVar> systemDomain){
		super(environmentDomain, systemDomain);
	}

}
