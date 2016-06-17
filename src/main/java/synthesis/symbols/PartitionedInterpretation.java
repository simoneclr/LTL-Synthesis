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
