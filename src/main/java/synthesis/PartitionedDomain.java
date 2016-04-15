package synthesis;

import formula.ltlf.LTLfLocalVar;

import java.util.HashSet;

/**
 * PartitionedDomain
 * <br>
 * Created by Simone Calciolari on 01/04/16.
 * @author Simone Calciolari.
 */
public class PartitionedDomain {

	private PropositionSet environmentDomain;
	private PropositionSet systemDomain;

	public PartitionedDomain(PropositionSet environmentDomain, PropositionSet systemDomain){

		for (LTLfLocalVar x : environmentDomain){
			if (systemDomain.contains(x)){
				throw new RuntimeException("Proposition " + x + " cannot appear in both system and environment domain");
			}
		}

		this.environmentDomain = environmentDomain;
		this.systemDomain = systemDomain;
	}

	@Override
	public boolean equals(Object o){
		if (o != null){
			if (o instanceof PartitionedDomain){
				PartitionedDomain other = (PartitionedDomain) o;

				return (this.getClass().equals(other.getClass())
								&& this.environmentDomain.equals(other.getEnvironmentDomain())
								&& this.systemDomain.equals(other.getSystemDomain()));
			}
		}

		return false;
	}

	@Override
	public String toString(){
		return "Environment: " + this.environmentDomain.toString() +
						"; System: " + this.systemDomain.toString();
	}

	public PropositionSet getCompleteDomain(){
		PropositionSet res = new PropositionSet();
		res.addAll(this.environmentDomain);
		res.addAll(this.systemDomain);
		return res;
	}

	public PropositionSet getEnvironmentDomain(){
		return environmentDomain;
	}

	public PropositionSet getSystemDomain(){
		return systemDomain;
	}
}
