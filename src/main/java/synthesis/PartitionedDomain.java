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

	private HashSet<LTLfLocalVar> environmentDomain;
	private HashSet<LTLfLocalVar> systemDomain;

	public PartitionedDomain(){
		this.environmentDomain = new HashSet<>();
		this.systemDomain = new HashSet<>();
	}

	public PartitionedDomain(HashSet<LTLfLocalVar> environmentDomain, HashSet<LTLfLocalVar> systemDomain){
		this.environmentDomain = environmentDomain;
		this.systemDomain = systemDomain;
	}

	public HashSet<LTLfLocalVar> getCompleteDomain(){
		HashSet<LTLfLocalVar> res = new HashSet<>();
		res.addAll(this.environmentDomain);
		res.addAll(this.systemDomain);
		return res;
	}

	public HashSet<LTLfLocalVar> getEnvironmentDomain(){
		return environmentDomain;
	}

	public HashSet<LTLfLocalVar> getSystemDomain(){
		return systemDomain;
	}

	public void setEnvironmentDomain(HashSet<LTLfLocalVar> environmentDomain){
		this.environmentDomain = environmentDomain;
	}

	public void setSystemDomain(HashSet<LTLfLocalVar> systemDomain){
		this.systemDomain = systemDomain;
	}
}
