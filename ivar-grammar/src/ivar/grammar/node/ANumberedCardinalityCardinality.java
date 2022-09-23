/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ANumberedCardinalityCardinality extends PCardinality
{
    private PNumberedCardinality _numberedCardinality_;

    public ANumberedCardinalityCardinality()
    {
        // Constructor
    }

    public ANumberedCardinalityCardinality(
        @SuppressWarnings("hiding") PNumberedCardinality _numberedCardinality_)
    {
        // Constructor
        setNumberedCardinality(_numberedCardinality_);

    }

    @Override
    public Object clone()
    {
        return new ANumberedCardinalityCardinality(
            cloneNode(this._numberedCardinality_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseANumberedCardinalityCardinality(this);
    }

    public PNumberedCardinality getNumberedCardinality()
    {
        return this._numberedCardinality_;
    }

    public void setNumberedCardinality(PNumberedCardinality node)
    {
        if(this._numberedCardinality_ != null)
        {
            this._numberedCardinality_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._numberedCardinality_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._numberedCardinality_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._numberedCardinality_ == child)
        {
            this._numberedCardinality_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._numberedCardinality_ == oldChild)
        {
            setNumberedCardinality((PNumberedCardinality) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
