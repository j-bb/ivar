/*
   Copyright 2020 Jean-Baptiste BRIAUD.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   imitations under the License.
 */
package ivar.compiler;

import ivar.helper.CollectionFactory;
import ivar.metamodel.target.generic.BusinessObject;
import ivar.metamodel.target.generic.BusinessObjectAttribute;
import java.util.List;
import java.util.Set;

public class BidirectionalRelation {

    private int fromLeftToRightUse;
    private int fromRightToLeftUse;
    private BusinessObject leftObject;
    private BusinessObjectAttribute leftAttribute;
    private BusinessObject rightObject;
    private BusinessObjectAttribute rightAttribute;
    private List<BidirectionalRelation> bidirectionalRelations = CollectionFactory.newList();

    public BidirectionalRelation(BusinessObject leftObject, BusinessObjectAttribute leftAttribute, BusinessObject rightObject, BusinessObjectAttribute rightAttribute) {
        this.leftObject = leftObject;
        this.leftAttribute = leftAttribute;
        this.rightObject = rightObject;
        this.rightAttribute = rightAttribute;
    }

    public List<BidirectionalRelation> getBidirectionalRelations() {
        return bidirectionalRelations;
    }

    public void addBidirectionalRelation(BidirectionalRelation bidirectionalRelation) {
        this.bidirectionalRelations.add(bidirectionalRelation);
    }

    public int getFromLeftToRightUse() {
        return fromLeftToRightUse;
    }

    public int getFromRightToLeftUse() {
        return fromRightToLeftUse;
    }

    public BusinessObject getLeftObject() {
        return leftObject;
    }

    public BusinessObjectAttribute getLeftAttribute() {
        return leftAttribute;
    }

    public BusinessObject getRightObject() {
        return rightObject;
    }

    public BusinessObjectAttribute getRightAttribute() {
        return rightAttribute;
    }

    public boolean istheSame(BidirectionalRelation bidirectionalRelation) {
        boolean result = false;
        if (leftObject.equals(bidirectionalRelation.getLeftObject()) && rightObject.equals(bidirectionalRelation.getRightObject())) {
            boolean sameLeftObject = leftObject.getFullName().equals(bidirectionalRelation.getLeftObject().getFullName());
            boolean sameRightObject = rightObject.getFullName().equals(bidirectionalRelation.getRightObject().getFullName());
            boolean sameLeftName = leftAttribute.getKeyname().equals(bidirectionalRelation.getLeftAttribute().getKeyname());
            boolean sameRightName = rightAttribute.getKeyname().equals(bidirectionalRelation.getRightAttribute().getKeyname());
            result = sameLeftObject && sameRightObject && sameLeftName && sameRightName;

        } else if (leftObject.equals(bidirectionalRelation.getRightObject()) && rightObject.equals(bidirectionalRelation.getLeftObject())) {
            boolean sameLeftObject = leftObject.getFullName().equals(bidirectionalRelation.getRightObject().getFullName());
            boolean sameRightObject = rightObject.getFullName().equals(bidirectionalRelation.getLeftObject().getFullName());
            boolean sameLeftName = leftAttribute.getKeyname().equals(bidirectionalRelation.getRightAttribute().getKeyname());
            boolean sameRightName = rightAttribute.getKeyname().equals(bidirectionalRelation.getLeftAttribute().getKeyname());
            result = sameLeftObject && sameRightObject && sameLeftName && sameRightName;
        }

        return result;
    }

    public static BidirectionalRelation bidirectionalRelationAlreadyThere(Set<BidirectionalRelation> bidirectionalRelations, final BidirectionalRelation bidirectionalRelation) {
        BidirectionalRelation result = null;
        for (final BidirectionalRelation relation : bidirectionalRelations) {
            if (bidirectionalRelation.istheSame(relation)) {
                result = relation;
                break;
            }
        }

        return result;
    }

}
