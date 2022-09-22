/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class TNumberTypeKeyword extends Token
{
    public TNumberTypeKeyword()
    {
        super.setText("Number");
    }

    public TNumberTypeKeyword(int line, int pos)
    {
        super.setText("Number");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TNumberTypeKeyword(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTNumberTypeKeyword(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TNumberTypeKeyword text.");
    }
}
