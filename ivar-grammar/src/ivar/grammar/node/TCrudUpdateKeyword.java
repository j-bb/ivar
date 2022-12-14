/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class TCrudUpdateKeyword extends Token
{
    public TCrudUpdateKeyword()
    {
        super.setText("update");
    }

    public TCrudUpdateKeyword(int line, int pos)
    {
        super.setText("update");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TCrudUpdateKeyword(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTCrudUpdateKeyword(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TCrudUpdateKeyword text.");
    }
}
