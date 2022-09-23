/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class TPercentileTypeKeyword extends Token
{
    public TPercentileTypeKeyword()
    {
        super.setText("Percentile");
    }

    public TPercentileTypeKeyword(int line, int pos)
    {
        super.setText("Percentile");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TPercentileTypeKeyword(getLine(), getPos());
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTPercentileTypeKeyword(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TPercentileTypeKeyword text.");
    }
}