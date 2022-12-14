/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class AIntegerIvarStepType extends PIvarStepType
{
    private TIntegerTypeKeyword _integerTypeKeyword_;

    public AIntegerIvarStepType()
    {
        // Constructor
    }

    public AIntegerIvarStepType(
        @SuppressWarnings("hiding") TIntegerTypeKeyword _integerTypeKeyword_)
    {
        // Constructor
        setIntegerTypeKeyword(_integerTypeKeyword_);

    }

    @Override
    public Object clone()
    {
        return new AIntegerIvarStepType(
            cloneNode(this._integerTypeKeyword_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAIntegerIvarStepType(this);
    }

    public TIntegerTypeKeyword getIntegerTypeKeyword()
    {
        return this._integerTypeKeyword_;
    }

    public void setIntegerTypeKeyword(TIntegerTypeKeyword node)
    {
        if(this._integerTypeKeyword_ != null)
        {
            this._integerTypeKeyword_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._integerTypeKeyword_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._integerTypeKeyword_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._integerTypeKeyword_ == child)
        {
            this._integerTypeKeyword_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._integerTypeKeyword_ == oldChild)
        {
            setIntegerTypeKeyword((TIntegerTypeKeyword) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
