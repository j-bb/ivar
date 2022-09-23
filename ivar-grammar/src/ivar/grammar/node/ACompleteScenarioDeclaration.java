/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ACompleteScenarioDeclaration extends PCompleteScenarioDeclaration
{
    private TScenarioKeyword _scenarioKeyword_;
    private PName _scenarioKeyname_;
    private TStringLiteral _scenarioName_;
    private PCrudModifier _crudModifier_;
    private PName _scenarioData_;
    private TLBrc _lBrc_;
    private PScenarioElements _scenarioElements_;
    private TRBrc _rBrc_;

    public ACompleteScenarioDeclaration()
    {
        // Constructor
    }

    public ACompleteScenarioDeclaration(
        @SuppressWarnings("hiding") TScenarioKeyword _scenarioKeyword_,
        @SuppressWarnings("hiding") PName _scenarioKeyname_,
        @SuppressWarnings("hiding") TStringLiteral _scenarioName_,
        @SuppressWarnings("hiding") PCrudModifier _crudModifier_,
        @SuppressWarnings("hiding") PName _scenarioData_,
        @SuppressWarnings("hiding") TLBrc _lBrc_,
        @SuppressWarnings("hiding") PScenarioElements _scenarioElements_,
        @SuppressWarnings("hiding") TRBrc _rBrc_)
    {
        // Constructor
        setScenarioKeyword(_scenarioKeyword_);

        setScenarioKeyname(_scenarioKeyname_);

        setScenarioName(_scenarioName_);

        setCrudModifier(_crudModifier_);

        setScenarioData(_scenarioData_);

        setLBrc(_lBrc_);

        setScenarioElements(_scenarioElements_);

        setRBrc(_rBrc_);

    }

    @Override
    public Object clone()
    {
        return new ACompleteScenarioDeclaration(
            cloneNode(this._scenarioKeyword_),
            cloneNode(this._scenarioKeyname_),
            cloneNode(this._scenarioName_),
            cloneNode(this._crudModifier_),
            cloneNode(this._scenarioData_),
            cloneNode(this._lBrc_),
            cloneNode(this._scenarioElements_),
            cloneNode(this._rBrc_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseACompleteScenarioDeclaration(this);
    }

    public TScenarioKeyword getScenarioKeyword()
    {
        return this._scenarioKeyword_;
    }

    public void setScenarioKeyword(TScenarioKeyword node)
    {
        if(this._scenarioKeyword_ != null)
        {
            this._scenarioKeyword_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._scenarioKeyword_ = node;
    }

    public PName getScenarioKeyname()
    {
        return this._scenarioKeyname_;
    }

    public void setScenarioKeyname(PName node)
    {
        if(this._scenarioKeyname_ != null)
        {
            this._scenarioKeyname_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._scenarioKeyname_ = node;
    }

    public TStringLiteral getScenarioName()
    {
        return this._scenarioName_;
    }

    public void setScenarioName(TStringLiteral node)
    {
        if(this._scenarioName_ != null)
        {
            this._scenarioName_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._scenarioName_ = node;
    }

    public PCrudModifier getCrudModifier()
    {
        return this._crudModifier_;
    }

    public void setCrudModifier(PCrudModifier node)
    {
        if(this._crudModifier_ != null)
        {
            this._crudModifier_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._crudModifier_ = node;
    }

    public PName getScenarioData()
    {
        return this._scenarioData_;
    }

    public void setScenarioData(PName node)
    {
        if(this._scenarioData_ != null)
        {
            this._scenarioData_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._scenarioData_ = node;
    }

    public TLBrc getLBrc()
    {
        return this._lBrc_;
    }

    public void setLBrc(TLBrc node)
    {
        if(this._lBrc_ != null)
        {
            this._lBrc_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._lBrc_ = node;
    }

    public PScenarioElements getScenarioElements()
    {
        return this._scenarioElements_;
    }

    public void setScenarioElements(PScenarioElements node)
    {
        if(this._scenarioElements_ != null)
        {
            this._scenarioElements_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._scenarioElements_ = node;
    }

    public TRBrc getRBrc()
    {
        return this._rBrc_;
    }

    public void setRBrc(TRBrc node)
    {
        if(this._rBrc_ != null)
        {
            this._rBrc_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._rBrc_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._scenarioKeyword_)
            + toString(this._scenarioKeyname_)
            + toString(this._scenarioName_)
            + toString(this._crudModifier_)
            + toString(this._scenarioData_)
            + toString(this._lBrc_)
            + toString(this._scenarioElements_)
            + toString(this._rBrc_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._scenarioKeyword_ == child)
        {
            this._scenarioKeyword_ = null;
            return;
        }

        if(this._scenarioKeyname_ == child)
        {
            this._scenarioKeyname_ = null;
            return;
        }

        if(this._scenarioName_ == child)
        {
            this._scenarioName_ = null;
            return;
        }

        if(this._crudModifier_ == child)
        {
            this._crudModifier_ = null;
            return;
        }

        if(this._scenarioData_ == child)
        {
            this._scenarioData_ = null;
            return;
        }

        if(this._lBrc_ == child)
        {
            this._lBrc_ = null;
            return;
        }

        if(this._scenarioElements_ == child)
        {
            this._scenarioElements_ = null;
            return;
        }

        if(this._rBrc_ == child)
        {
            this._rBrc_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._scenarioKeyword_ == oldChild)
        {
            setScenarioKeyword((TScenarioKeyword) newChild);
            return;
        }

        if(this._scenarioKeyname_ == oldChild)
        {
            setScenarioKeyname((PName) newChild);
            return;
        }

        if(this._scenarioName_ == oldChild)
        {
            setScenarioName((TStringLiteral) newChild);
            return;
        }

        if(this._crudModifier_ == oldChild)
        {
            setCrudModifier((PCrudModifier) newChild);
            return;
        }

        if(this._scenarioData_ == oldChild)
        {
            setScenarioData((PName) newChild);
            return;
        }

        if(this._lBrc_ == oldChild)
        {
            setLBrc((TLBrc) newChild);
            return;
        }

        if(this._scenarioElements_ == oldChild)
        {
            setScenarioElements((PScenarioElements) newChild);
            return;
        }

        if(this._rBrc_ == oldChild)
        {
            setRBrc((TRBrc) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}