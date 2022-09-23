/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ATypeImportStarDeclaration extends PTypeImportStarDeclaration
{
    private TImportKeyword _importKeyword_;
    private PName _name_;
    private TDot _dot_;
    private TStar _star_;
    private TSemi _semi_;

    public ATypeImportStarDeclaration()
    {
        // Constructor
    }

    public ATypeImportStarDeclaration(
        @SuppressWarnings("hiding") TImportKeyword _importKeyword_,
        @SuppressWarnings("hiding") PName _name_,
        @SuppressWarnings("hiding") TDot _dot_,
        @SuppressWarnings("hiding") TStar _star_,
        @SuppressWarnings("hiding") TSemi _semi_)
    {
        // Constructor
        setImportKeyword(_importKeyword_);

        setName(_name_);

        setDot(_dot_);

        setStar(_star_);

        setSemi(_semi_);

    }

    @Override
    public Object clone()
    {
        return new ATypeImportStarDeclaration(
            cloneNode(this._importKeyword_),
            cloneNode(this._name_),
            cloneNode(this._dot_),
            cloneNode(this._star_),
            cloneNode(this._semi_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseATypeImportStarDeclaration(this);
    }

    public TImportKeyword getImportKeyword()
    {
        return this._importKeyword_;
    }

    public void setImportKeyword(TImportKeyword node)
    {
        if(this._importKeyword_ != null)
        {
            this._importKeyword_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._importKeyword_ = node;
    }

    public PName getName()
    {
        return this._name_;
    }

    public void setName(PName node)
    {
        if(this._name_ != null)
        {
            this._name_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._name_ = node;
    }

    public TDot getDot()
    {
        return this._dot_;
    }

    public void setDot(TDot node)
    {
        if(this._dot_ != null)
        {
            this._dot_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._dot_ = node;
    }

    public TStar getStar()
    {
        return this._star_;
    }

    public void setStar(TStar node)
    {
        if(this._star_ != null)
        {
            this._star_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._star_ = node;
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
            + toString(this._importKeyword_)
            + toString(this._name_)
            + toString(this._dot_)
            + toString(this._star_)
            + toString(this._semi_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._importKeyword_ == child)
        {
            this._importKeyword_ = null;
            return;
        }

        if(this._name_ == child)
        {
            this._name_ = null;
            return;
        }

        if(this._dot_ == child)
        {
            this._dot_ = null;
            return;
        }

        if(this._star_ == child)
        {
            this._star_ = null;
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
        if(this._importKeyword_ == oldChild)
        {
            setImportKeyword((TImportKeyword) newChild);
            return;
        }

        if(this._name_ == oldChild)
        {
            setName((PName) newChild);
            return;
        }

        if(this._dot_ == oldChild)
        {
            setDot((TDot) newChild);
            return;
        }

        if(this._star_ == oldChild)
        {
            setStar((TStar) newChild);
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
