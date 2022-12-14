/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ARoleRoleList extends PRoleList
{
    private PRole _role_;

    public ARoleRoleList()
    {
        // Constructor
    }

    public ARoleRoleList(
        @SuppressWarnings("hiding") PRole _role_)
    {
        // Constructor
        setRole(_role_);

    }

    @Override
    public Object clone()
    {
        return new ARoleRoleList(
            cloneNode(this._role_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseARoleRoleList(this);
    }

    public PRole getRole()
    {
        return this._role_;
    }

    public void setRole(PRole node)
    {
        if(this._role_ != null)
        {
            this._role_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._role_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._role_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._role_ == child)
        {
            this._role_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._role_ == oldChild)
        {
            setRole((PRole) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
