package core.tableGenerator;

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


import java.util.List;

import basicDatatypes.Schema;

public abstract class Generator {
	
	public abstract List<Schema> pumpTable(int nRows, Schema schema);

	public abstract void setPureRandomGeneration();
	
//	public abstract float findNullRatio(Schema s, ColumnPumper column);
//	
//	public abstract float findDuplicateRatio(Schema s, ColumnPumper column);



}
