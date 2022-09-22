/*
   Copyright (c) 2004-2020, Jean-Baptiste BRIAUD. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License
 */
package ivar.ivarc;

import ivar.grammar.analysis.ReversedDepthFirstAdapter;
import ivar.grammar.node.Node;
import ivar.grammar.node.Token;
import ivar.helper.CollectionFactory;
import java.util.Map;

public class IvarFilePositionsVisitor extends ReversedDepthFirstAdapter {

    // line/column -1 means "end of file"
    private int currentLine = -1;
    private int currentColumn = -1;

    private Map<Node, Integer> lines = CollectionFactory.newMap();
    private Map<Node, Integer> columns = CollectionFactory.newMap();

    @Override
    public void defaultIn(final Node node) {
        lines.put(node, currentLine - 1);
        columns.put(node, currentColumn);
    }

    @Override
    public void defaultCase(final Node node) {
        if (node instanceof Token) {
            final Token token = (Token) node;
            currentLine = token.getLine();
            currentColumn = token.getPos();
        }
    }

    public String getPositionFromNode(final Node node) {
        final Integer line = lines.get(node);
        final Integer column = columns.get(node);
        final String lineString = (line == null) ? "<no line>" : line.toString();
        final String columnString = (column == null) ? "<no column>" : column.toString();

        return "[line " + lineString + ", col " + columnString + "]";
    }

    public int getNodesNumber() {
        return lines.size();
    }

    public void freeBeforeCompile() {
        lines.clear();
        columns.clear();
        lines = null;
        columns = null;
    }
}
