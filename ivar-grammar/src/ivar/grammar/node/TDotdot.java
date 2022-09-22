/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class TDotdot extends Token
{
    public TDotdot()
    {
        super.setText("..");
    }

    public TDotdot(int line, int pos)
    {
        super.setText("..");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TDotdot(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTDotdot(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TDotdot text.");
    }
}
