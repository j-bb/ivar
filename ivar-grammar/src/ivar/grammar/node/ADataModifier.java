/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ADataModifier extends PDataModifier
{
    private TDataModifierDefaultKeyword _dataModifierDefaultKeyword_;

    public ADataModifier()
    {
        // Constructor
    }

    public ADataModifier(
        @SuppressWarnings("hiding") TDataModifierDefaultKeyword _dataModifierDefaultKeyword_)
    {
        // Constructor
        setDataModifierDefaultKeyword(_dataModifierDefaultKeyword_);

    }

    @Override
    public Object clone()
    {
        return new ADataModifier(
            cloneNode(this._dataModifierDefaultKeyword_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseADataModifier(this);
    }

    public TDataModifierDefaultKeyword getDataModifierDefaultKeyword()
    {
        return this._dataModifierDefaultKeyword_;
    }

    public void setDataModifierDefaultKeyword(TDataModifierDefaultKeyword node)
    {
        if(this._dataModifierDefaultKeyword_ != null)
        {
            this._dataModifierDefaultKeyword_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._dataModifierDefaultKeyword_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._dataModifierDefaultKeyword_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._dataModifierDefaultKeyword_ == child)
        {
            this._dataModifierDefaultKeyword_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._dataModifierDefaultKeyword_ == oldChild)
        {
            setDataModifierDefaultKeyword((TDataModifierDefaultKeyword) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
