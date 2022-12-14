/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class TCrudCreateKeyword extends Token
{
    public TCrudCreateKeyword()
    {
        super.setText("create");
    }

    public TCrudCreateKeyword(int line, int pos)
    {
        super.setText("create");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TCrudCreateKeyword(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTCrudCreateKeyword(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TCrudCreateKeyword text.");
    }
}
