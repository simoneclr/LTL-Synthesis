package util;

import automaton.EmptyTrace;
import automaton.PossibleWorldWrap;
import automaton.TransitionLabel;
import formula.ldlf.LDLfFormula;
import formula.ltlf.LTLfFormula;
import formula.ltlf.LTLfLocalVar;
import net.sf.tweety.logics.pl.syntax.Proposition;
import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;
import rationals.transformations.Reducer;
import synthesis.symbols.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * AutomatonUtils
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
public class AutomatonUtils {

	public static Automaton buildLTLfAutomaton(LTLfFormula formula){
		LDLfFormula ldLfFormula = formula.toLDLf();
		return new Reducer<>().transform(utils.AutomatonUtils.ldlf2Automaton(ldLfFormula, ldLfFormula.getSignature()));
	}

	public static Automaton removeUnreachableStates(Automaton original){
		Automaton res = new Automaton();

		Set<State> initials = original.initials();
		HashMap<State, State> oldToNewStates = new HashMap<>();

		for (State i : initials){
			State newI = res.addState(i.isInitial(), i.isTerminal());
			oldToNewStates.put(i, newI);
		}

		Set<State> toBeVisited = new HashSet<>();
		toBeVisited.addAll(initials);

		while (!toBeVisited.isEmpty()){
			State oldStart = toBeVisited.iterator().next();
			toBeVisited.remove(oldStart);

			HashSet<State> newTBV = new HashSet<>();

			Set<Transition> oldTransitions = original.delta(oldStart);

			for (Transition oldT : oldTransitions){
				State oldEnd = oldT.end();
				State newEnd = oldToNewStates.get(oldEnd);

				if (newEnd == null){
					newEnd = res.addState(oldEnd.isInitial(), oldEnd.isTerminal());
					oldToNewStates.put(oldEnd, newEnd);
					newTBV.add(oldEnd);
				}

				Transition newT = new Transition(oldToNewStates.get(oldStart), oldT.label(), newEnd);

				try {
					res.addTransition(newT);
				} catch (NoSuchStateException e) {
					e.printStackTrace();
				}
			}

			toBeVisited.addAll(newTBV);
		}

		return res;
	}

	public static Automaton transalteToGameAutomaton(Automaton original, PartitionedDomain domain){
		Automaton res = new Automaton();

		//Remove emptyTrace transitions
		original = utils.AutomatonUtils.eliminateEmptyTrace(original);

		original = removeUnreachableStates(original);

		PropositionSet environment = domain.getEnvironmentDomain();
		PropositionSet system = domain.getSystemDomain();

		//Get original states iterator
		Iterator<State> originalStates = original.states().iterator();
		//Map to translate states
		HashMap<State, State> oldToNewStates = new HashMap<>();

		//Add states to the new automaton and fill the map
		while (originalStates.hasNext()){
			State oldState = originalStates.next();
			State newState = res.addState(oldState.isInitial(), oldState.isTerminal());
			oldToNewStates.put(oldState, newState);
		}

		//ADDING TRANSITION TO NEW AUTOMATON
		//For each original state, get all transitions starting from it, translate the label,
		//and insert the new translated transition in the translated automaton

		//Get the iterator on the original states (again)
		originalStates = original.states().iterator();

		while (originalStates.hasNext()){

			State oldStart = originalStates.next();
			Set<Transition<TransitionLabel>> oldTransitions = original.delta(oldStart);

			//Iterate over all transition starting from the current (old) state.
			for (Transition<TransitionLabel> oldTransition : oldTransitions){

				//Get end state
				State oldEnd = oldTransition.end();
				//Get old label
				TransitionLabel oldLabel = oldTransition.label();

				//New label
				SynthTransitionLabel newLabel;

				if (oldLabel instanceof EmptyTrace){
					newLabel = new SynthEmptyTrace();
				} else if (oldLabel instanceof PossibleWorldWrap){
					newLabel = partitionPossibleWorld((PossibleWorldWrap) oldLabel, domain);
				} else {
					throw new RuntimeException("Unknown label type");
				}

				//Create new transition
				//Get start and end states
				State newStart = oldToNewStates.get(oldStart);
				State newEnd = oldToNewStates.get(oldEnd);

				Transition<SynthTransitionLabel> newTransition = new Transition<>(newStart, newLabel, newEnd);

				//Add it to translated automaton
				try {
					res.addTransition(newTransition);
				} catch (NoSuchStateException e){
					throw new RuntimeException(e);
				}
			}
		}

		return res;
	}

	public static void writeAutomatonGv(Automaton automaton, String fileName){
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintStream ps = new PrintStream(fos);
		ps.println(utils.AutomatonUtils.toDot(automaton));
		ps.flush();
		ps.close();
	}

	private static PartitionedInterpretation partitionPossibleWorld(PossibleWorldWrap pw, PartitionedDomain domain){
		Interpretation environment = new Interpretation();
		Interpretation system = new Interpretation();

		for (Proposition p : pw){
			LTLfLocalVar lv = new LTLfLocalVar(p);

			if (domain.getEnvironmentDomain().contains(lv)){
				environment.add(lv);
			} else if (domain.getSystemDomain().contains(lv)) {
				system.add(lv);
			} else {
				throw new RuntimeException("Found propositional variable not declared in domain");
			}
		}

		return new PartitionedInterpretation(environment, system);
	}
}
