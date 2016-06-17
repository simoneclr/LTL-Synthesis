package synthesis.symbols;

/**
 * StrategySuccessOutput
 * <br>
 * Created by Simone Calciolari on 15/06/16.
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
public class StrategySuccessOutput implements StrategyOutput{
	@Override
	public String toString(){
		return "SUCCESS";
	}
}
