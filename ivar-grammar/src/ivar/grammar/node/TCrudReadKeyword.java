/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class TCrudReadKeyword extends Token
{
    public TCrudReadKeyword()
    {
        super.setText("read");
    }

    public TCrudReadKeyword(int line, int pos)
    {
        super.setText("read");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TCrudReadKeyword(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTCrudReadKeyword(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TCrudReadKeyword text.");
    }
}
