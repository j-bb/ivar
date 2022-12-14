/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ASimpleShadowStepDeclaration extends PSimpleShadowStepDeclaration
{
    private PName _stepKeyname_;
    private TSemi _semi_;

    public ASimpleShadowStepDeclaration()
    {
        // Constructor
    }

    public ASimpleShadowStepDeclaration(
        @SuppressWarnings("hiding") PName _stepKeyname_,
        @SuppressWarnings("hiding") TSemi _semi_)
    {
        // Constructor
        setStepKeyname(_stepKeyname_);

        setSemi(_semi_);

    }

    @Override
    public Object clone()
    {
        return new ASimpleShadowStepDeclaration(
            cloneNode(this._stepKeyname_),
            cloneNode(this._semi_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseASimpleShadowStepDeclaration(this);
    }

    public PName getStepKeyname()
    {
        return this._stepKeyname_;
    }

    public void setStepKeyname(PName node)
    {
        if(this._stepKeyname_ != null)
        {
            this._stepKeyname_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._stepKeyname_ = node;
    }

    public TSemi getSemi()
    {
        return this._semi_;
    }

    public void setSemi(TSemi node)
    {
        if(this._semi_ != null)
        {
            this._semi_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._semi_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._stepKeyname_)
            + toString(this._semi_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._stepKeyname_ == child)
        {
            this._stepKeyname_ = null;
            return;
        }

        if(this._semi_ == child)
        {
            this._semi_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._stepKeyname_ == oldChild)
        {
            setStepKeyname((PName) newChild);
            return;
        }

        if(this._semi_ == oldChild)
        {
            setSemi((TSemi) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
