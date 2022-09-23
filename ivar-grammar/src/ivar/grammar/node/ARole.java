/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ARole extends PRole
{
    private PRoleModifier _roleModifier_;
    private PName _roleKeyname_;
    private TStringLiteral _roleName_;
    private TSemi _semi_;

    public ARole()
    {
        // Constructor
    }

    public ARole(
        @SuppressWarnings("hiding") PRoleModifier _roleModifier_,
        @SuppressWarnings("hiding") PName _roleKeyname_,
        @SuppressWarnings("hiding") TStringLiteral _roleName_,
        @SuppressWarnings("hiding") TSemi _semi_)
    {
        // Constructor
        setRoleModifier(_roleModifier_);

        setRoleKeyname(_roleKeyname_);

        setRoleName(_roleName_);

        setSemi(_semi_);

    }

    @Override
    public Object clone()
    {
        return new ARole(
            cloneNode(this._roleModifier_),
            cloneNode(this._roleKeyname_),
            cloneNode(this._roleName_),
            cloneNode(this._semi_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseARole(this);
    }

    public PRoleModifier getRoleModifier()
    {
        return this._roleModifier_;
    }

    public void setRoleModifier(PRoleModifier node)
    {
        if(this._roleModifier_ != null)
        {
            this._roleModifier_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._roleModifier_ = node;
    }

    public PName getRoleKeyname()
    {
        return this._roleKeyname_;
    }

    public void setRoleKeyname(PName node)
    {
        if(this._roleKeyname_ != null)
        {
            this._roleKeyname_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._roleKeyname_ = node;
    }

    public TStringLiteral getRoleName()
    {
        return this._roleName_;
    }

    public void setRoleName(TStringLiteral node)
    {
        if(this._roleName_ != null)
        {
            this._roleName_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._roleName_ = node;
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
            + toString(this._roleModifier_)
            + toString(this._roleKeyname_)
            + toString(this._roleName_)
            + toString(this._semi_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._roleModifier_ == child)
        {
            this._roleModifier_ = null;
            return;
        }

        if(this._roleKeyname_ == child)
        {
            this._roleKeyname_ = null;
            return;
        }

        if(this._roleName_ == child)
        {
            this._roleName_ = null;
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
        if(this._roleModifier_ == oldChild)
        {
            setRoleModifier((PRoleModifier) newChild);
            return;
        }

        if(this._roleKeyname_ == oldChild)
        {
            setRoleKeyname((PName) newChild);
            return;
        }

        if(this._roleName_ == oldChild)
        {
            setRoleName((TStringLiteral) newChild);
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