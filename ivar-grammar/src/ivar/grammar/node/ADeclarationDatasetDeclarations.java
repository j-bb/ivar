/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ADeclarationDatasetDeclarations extends PDatasetDeclarations
{
    private PDatasetDeclaration _datasetDeclaration_;

    public ADeclarationDatasetDeclarations()
    {
        // Constructor
    }

    public ADeclarationDatasetDeclarations(
        @SuppressWarnings("hiding") PDatasetDeclaration _datasetDeclaration_)
    {
        // Constructor
        setDatasetDeclaration(_datasetDeclaration_);

    }

    @Override
    public Object clone()
    {
        return new ADeclarationDatasetDeclarations(
            cloneNode(this._datasetDeclaration_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseADeclarationDatasetDeclarations(this);
    }

    public PDatasetDeclaration getDatasetDeclaration()
    {
        return this._datasetDeclaration_;
    }

    public void setDatasetDeclaration(PDatasetDeclaration node)
    {
        if(this._datasetDeclaration_ != null)
        {
            this._datasetDeclaration_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._datasetDeclaration_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._datasetDeclaration_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._datasetDeclaration_ == child)
        {
            this._datasetDeclaration_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._datasetDeclaration_ == oldChild)
        {
            setDatasetDeclaration((PDatasetDeclaration) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
