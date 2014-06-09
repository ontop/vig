package utils;

/*
 * #%L
 * dataPumper
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public class Pair<T,S> {
	public final T first;
	public final S second;
	
	public Pair(T first, S second){
		this.first = first;
		this.second = second;
	}
	
	@Override 
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof Pair) {
			Pair that = (Pair) other;
			result = (this.first == that.first && this.second == that.second);
		}
		return result;
	}
	
	@Override public int hashCode() {
		return (41 * (41 + this.first.hashCode()) + this.second.hashCode());
	}
	
	public String toString(){
		return "["+first.toString()+", "+second.toString()+"]";
	}
};
