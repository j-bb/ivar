/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ADeclarationsScenarioDeclarations extends PScenarioDeclarations
{
    private PScenarioDeclarations _scenarioDeclarations_;
    private PScenarioDeclaration _scenarioDeclaration_;

    public ADeclarationsScenarioDeclarations()
    {
        // Constructor
    }

    public ADeclarationsScenarioDeclarations(
        @SuppressWarnings("hiding") PScenarioDeclarations _scenarioDeclarations_,
        @SuppressWarnings("hiding") PScenarioDeclaration _scenarioDeclaration_)
    {
        // Constructor
        setScenarioDeclarations(_scenarioDeclarations_);

        setScenarioDeclaration(_scenarioDeclaration_);

    }

    @Override
    public Object clone()
    {
        return new ADeclarationsScenarioDeclarations(
            cloneNode(this._scenarioDeclarations_),
            cloneNode(this._scenarioDeclaration_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseADeclarationsScenarioDeclarations(this);
    }

    public PScenarioDeclarations getScenarioDeclarations()
    {
        return this._scenarioDeclarations_;
    }

    public void setScenarioDeclarations(PScenarioDeclarations node)
    {
        if(this._scenarioDeclarations_ != null)
        {
            this._scenarioDeclarations_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._scenarioDeclarations_ = node;
    }

    public PScenarioDeclaration getScenarioDeclaration()
    {
        return this._scenarioDeclaration_;
    }

    public void setScenarioDeclaration(PScenarioDeclaration node)
    {
        if(this._scenarioDeclaration_ != null)
        {
            this._scenarioDeclaration_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._scenarioDeclaration_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._scenarioDeclarations_)
            + toString(this._scenarioDeclaration_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._scenarioDeclarations_ == child)
        {
            this._scenarioDeclarations_ = null;
            return;
        }

        if(this._scenarioDeclaration_ == child)
        {
            this._scenarioDeclaration_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._scenarioDeclarations_ == oldChild)
        {
            setScenarioDeclarations((PScenarioDeclarations) newChild);
            return;
        }

        if(this._scenarioDeclaration_ == oldChild)
        {
            setScenarioDeclaration((PScenarioDeclaration) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
