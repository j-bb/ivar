/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ADeclarationImportDeclarations extends PImportDeclarations
{
    private PImportDeclaration _importDeclaration_;

    public ADeclarationImportDeclarations()
    {
        // Constructor
    }

    public ADeclarationImportDeclarations(
        @SuppressWarnings("hiding") PImportDeclaration _importDeclaration_)
    {
        // Constructor
        setImportDeclaration(_importDeclaration_);

    }

    @Override
    public Object clone()
    {
        return new ADeclarationImportDeclarations(
            cloneNode(this._importDeclaration_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseADeclarationImportDeclarations(this);
    }

    public PImportDeclaration getImportDeclaration()
    {
        return this._importDeclaration_;
    }

    public void setImportDeclaration(PImportDeclaration node)
    {
        if(this._importDeclaration_ != null)
        {
            this._importDeclaration_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._importDeclaration_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._importDeclaration_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._importDeclaration_ == child)
        {
            this._importDeclaration_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._importDeclaration_ == oldChild)
        {
            setImportDeclaration((PImportDeclaration) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}