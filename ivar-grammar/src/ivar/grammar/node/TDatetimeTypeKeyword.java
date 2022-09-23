/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class TDatetimeTypeKeyword extends Token
{
    public TDatetimeTypeKeyword()
    {
        super.setText("DateTime");
    }

    public TDatetimeTypeKeyword(int line, int pos)
    {
        super.setText("DateTime");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TDatetimeTypeKeyword(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTDatetimeTypeKeyword(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TDatetimeTypeKeyword text.");
    }
}