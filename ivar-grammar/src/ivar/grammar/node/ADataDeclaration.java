/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ADataDeclaration extends PDataDeclaration
{
    private PDataModifier _dataModifier_;
    private PName _dataKeyname_;
    private TStringLiteral _dataValueInLang_;
    private TSemi _semi_;

    public ADataDeclaration()
    {
        // Constructor
    }

    public ADataDeclaration(
        @SuppressWarnings("hiding") PDataModifier _dataModifier_,
        @SuppressWarnings("hiding") PName _dataKeyname_,
        @SuppressWarnings("hiding") TStringLiteral _dataValueInLang_,
        @SuppressWarnings("hiding") TSemi _semi_)
    {
        // Constructor
        setDataModifier(_dataModifier_);

        setDataKeyname(_dataKeyname_);

        setDataValueInLang(_dataValueInLang_);

        setSemi(_semi_);

    }

    @Override
    public Object clone()
    {
        return new ADataDeclaration(
            cloneNode(this._dataModifier_),
            cloneNode(this._dataKeyname_),
            cloneNode(this._dataValueInLang_),
            cloneNode(this._semi_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseADataDeclaration(this);
    }

    public PDataModifier getDataModifier()
    {
        return this._dataModifier_;
    }

    public void setDataModifier(PDataModifier node)
    {
        if(this._dataModifier_ != null)
        {
            this._dataModifier_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._dataModifier_ = node;
    }

    public PName getDataKeyname()
    {
        return this._dataKeyname_;
    }

    public void setDataKeyname(PName node)
    {
        if(this._dataKeyname_ != null)
        {
            this._dataKeyname_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._dataKeyname_ = node;
    }

    public TStringLiteral getDataValueInLang()
    {
        return this._dataValueInLang_;
    }

    public void setDataValueInLang(TStringLiteral node)
    {
        if(this._dataValueInLang_ != null)
        {
            this._dataValueInLang_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._dataValueInLang_ = node;
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
            + toString(this._dataModifier_)
            + toString(this._dataKeyname_)
            + toString(this._dataValueInLang_)
            + toString(this._semi_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._dataModifier_ == child)
        {
            this._dataModifier_ = null;
            return;
        }

        if(this._dataKeyname_ == child)
        {
            this._dataKeyname_ = null;
            return;
        }

        if(this._dataValueInLang_ == child)
        {
            this._dataValueInLang_ = null;
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
        if(this._dataModifier_ == oldChild)
        {
            setDataModifier((PDataModifier) newChild);
            return;
        }

        if(this._dataKeyname_ == oldChild)
        {
            setDataKeyname((PName) newChild);
            return;
        }

        if(this._dataValueInLang_ == oldChild)
        {
            setDataValueInLang((TStringLiteral) newChild);
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
