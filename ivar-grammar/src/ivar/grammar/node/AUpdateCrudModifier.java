/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class AUpdateCrudModifier extends PCrudModifier
{
    private TCrudUpdateKeyword _crudUpdateKeyword_;

    public AUpdateCrudModifier()
    {
        // Constructor
    }

    public AUpdateCrudModifier(
        @SuppressWarnings("hiding") TCrudUpdateKeyword _crudUpdateKeyword_)
    {
        // Constructor
        setCrudUpdateKeyword(_crudUpdateKeyword_);

    }

    @Override
    public Object clone()
    {
        return new AUpdateCrudModifier(
            cloneNode(this._crudUpdateKeyword_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAUpdateCrudModifier(this);
    }

    public TCrudUpdateKeyword getCrudUpdateKeyword()
    {
        return this._crudUpdateKeyword_;
    }

    public void setCrudUpdateKeyword(TCrudUpdateKeyword node)
    {
        if(this._crudUpdateKeyword_ != null)
        {
            this._crudUpdateKeyword_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._crudUpdateKeyword_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._crudUpdateKeyword_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._crudUpdateKeyword_ == child)
        {
            this._crudUpdateKeyword_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._crudUpdateKeyword_ == oldChild)
        {
            setCrudUpdateKeyword((TCrudUpdateKeyword) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}