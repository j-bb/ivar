/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class TDotdotstar extends Token
{
    public TDotdotstar()
    {
        super.setText("..*");
    }

    public TDotdotstar(int line, int pos)
    {
        super.setText("..*");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TDotdotstar(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTDotdotstar(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TDotdotstar text.");
    }
}
