/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class TIntegerTypeKeyword extends Token
{
    public TIntegerTypeKeyword()
    {
        super.setText("Integer");
    }

    public TIntegerTypeKeyword(int line, int pos)
    {
        super.setText("Integer");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TIntegerTypeKeyword(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTIntegerTypeKeyword(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TIntegerTypeKeyword text.");
    }
}
