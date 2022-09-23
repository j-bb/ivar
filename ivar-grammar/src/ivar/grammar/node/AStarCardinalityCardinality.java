/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class AStarCardinalityCardinality extends PCardinality
{
    private PStarCardinality _starCardinality_;

    public AStarCardinalityCardinality()
    {
        // Constructor
    }

    public AStarCardinalityCardinality(
        @SuppressWarnings("hiding") PStarCardinality _starCardinality_)
    {
        // Constructor
        setStarCardinality(_starCardinality_);

    }

    @Override
    public Object clone()
    {
        return new AStarCardinalityCardinality(
            cloneNode(this._starCardinality_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAStarCardinalityCardinality(this);
    }

    public PStarCardinality getStarCardinality()
    {
        return this._starCardinality_;
    }

    public void setStarCardinality(PStarCardinality node)
    {
        if(this._starCardinality_ != null)
        {
            this._starCardinality_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._starCardinality_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._starCardinality_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._starCardinality_ == child)
        {
            this._starCardinality_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._starCardinality_ == oldChild)
        {
            setStarCardinality((PStarCardinality) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
