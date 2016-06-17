package synthesis.symbols;

import formula.ltlf.LTLfLocalVar;

/**
 * Class that represents the domain partitioned into proposition controlled by the environment and
 * propositions controlled by the system
 * <br>
 * Created by Simone Calciolari on 01/04/16.
 * @author Simone Calciolari.
 *
 * LTL-Synthesis. Perform LTL Synthesis on finite traces. Copyright (C) 2016 Simone Calciolari
 *
 * This file is part of LTL-Synthesis.
 *
 * LTL-Synthesis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LTL-Synthesis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LTL-Synthesis. If not, see <http://www.gnu.org/licenses/>.
 */
public class PartitionedDomain {

	private PropositionSet environmentDomain;
	private PropositionSet systemDomain;

	/**
	 * Instantiates a new PartitionedDomain
	 * @param environmentDomain the propositions controlled by the environment
	 * @param systemDomain the propositions controlled by the system
	 */
	public PartitionedDomain(PropositionSet environmentDomain, PropositionSet systemDomain){

		for (LTLfLocalVar x : environmentDomain){
			if (systemDomain.contains(x)){
				throw new RuntimeException("System and environment domain must be disjoint; " +
						"Proposition " + x + " appears in both.");
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

	/**
	 * Retrieves the union of the system and environment partitions
	 * @return a PropositionSet containing all the proposition in the domain
	 */
	public PropositionSet getCompleteDomain(){
		PropositionSet res = new PropositionSet();
		res.addAll(this.environmentDomain);
		res.addAll(this.systemDomain);
		return res;
	}

	/**
	 * Retrieves the propositions of the domain controlled by the environment
	 * @return a PropositionSet containing the propositions controlled by the environment
	 */
	public PropositionSet getEnvironmentDomain(){
		return environmentDomain;
	}

	/**
	 * Retrieves the propositions of the domain controlled by the system
	 * @return a PropositionSet containing the propositions controlled by the system
	 */
	public PropositionSet getSystemDomain(){
		return systemDomain;
	}
}
