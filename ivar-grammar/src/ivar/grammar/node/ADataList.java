/* This file was generated by SableCC (http://www.sablecc.org/). */

package ivar.grammar.node;

import java.util.*;
import ivar.grammar.analysis.*;

@SuppressWarnings("nls")
public final class ADataList extends PDataList
{
    private final LinkedList<PDataDeclaration> _dataDeclaration_ = new LinkedList<PDataDeclaration>();

    public ADataList()
    {
        // Constructor
    }

    public ADataList(
        @SuppressWarnings("hiding") List<?> _dataDeclaration_)
    {
        // Constructor
        setDataDeclaration(_dataDeclaration_);

    }

    @Override
    public Object clone()
    {
        return new ADataList(
            cloneList(this._dataDeclaration_));
    }

    @Override
    public void apply(Switch sw)
    {
        ((Analysis) sw).caseADataList(this);
    }

    public LinkedList<PDataDeclaration> getDataDeclaration()
    {
        return this._dataDeclaration_;
    }

    public void setDataDeclaration(List<?> list)
    {
        for(PDataDeclaration e : this._dataDeclaration_)
        {
            e.parent(null);
        }
        this._dataDeclaration_.clear();

        for(Object obj_e : list)
        {
            PDataDeclaration e = (PDataDeclaration) obj_e;
            if(e.parent() != null)
            {
                e.parent().removeChild(e);
            }

            e.parent(this);
            this._dataDeclaration_.add(e);
        }
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._dataDeclaration_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._dataDeclaration_.remove(child))
        {
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        for(ListIterator<PDataDeclaration> i = this._dataDeclaration_.listIterator(); i.hasNext();)
        {
            if(i.next() == oldChild)
            {
                if(newChild != null)
                {
                    i.set((PDataDeclaration) newChild);
                    newChild.parent(this);
                    oldChild.parent(null);
                    return;
                }

                i.remove();
                oldChild.parent(null);
                return;
            }
        }

        throw new RuntimeException("Not a child.");
    }
}
