/*
 * Copyright (C) 2011-2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is auto-generated. DO NOT MODIFY!
 * The source Renderscript file: /home/alexis/AndroidStudioProjects/LWPWaterRipple/waterScreens/src/main/rs/fall.rs
 */

package com.harlan.waterscreen;

import android.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptField_Constants extends android.renderscript.Script.FieldBase {
    static public class Item {
        public static final int sizeof = 192;

        Float4 Drop01;
        Float4 Drop02;
        Float4 Drop03;
        Float4 Drop04;
        Float4 Drop05;
        Float4 Drop06;
        Float4 Drop07;
        Float4 Drop08;
        Float4 Drop09;
        Float4 Drop10;
        Float4 Offset;
        float Rotate;

        Item() {
            Drop01 = new Float4();
            Drop02 = new Float4();
            Drop03 = new Float4();
            Drop04 = new Float4();
            Drop05 = new Float4();
            Drop06 = new Float4();
            Drop07 = new Float4();
            Drop08 = new Float4();
            Drop09 = new Float4();
            Drop10 = new Float4();
            Offset = new Float4();
        }

    }

    private Item mItemArray[];
    private FieldPacker mIOBuffer;
    private static java.lang.ref.WeakReference<Element> mElementCache = new java.lang.ref.WeakReference<Element>(null);
    public static Element createElement(RenderScript rs) {
        Element.Builder eb = new Element.Builder(rs);
        eb.add(Element.F32_4(rs), "Drop01");
        eb.add(Element.F32_4(rs), "Drop02");
        eb.add(Element.F32_4(rs), "Drop03");
        eb.add(Element.F32_4(rs), "Drop04");
        eb.add(Element.F32_4(rs), "Drop05");
        eb.add(Element.F32_4(rs), "Drop06");
        eb.add(Element.F32_4(rs), "Drop07");
        eb.add(Element.F32_4(rs), "Drop08");
        eb.add(Element.F32_4(rs), "Drop09");
        eb.add(Element.F32_4(rs), "Drop10");
        eb.add(Element.F32_4(rs), "Offset");
        eb.add(Element.F32(rs), "Rotate");
        eb.add(Element.U32(rs), "#rs_padding_1");
        eb.add(Element.U32(rs), "#rs_padding_2");
        eb.add(Element.U32(rs), "#rs_padding_3");
        return eb.create();
    }

    private  ScriptField_Constants(RenderScript rs) {
        mItemArray = null;
        mIOBuffer = null;
        mElement = createElement(rs);
    }

    public  ScriptField_Constants(RenderScript rs, int count) {
        mItemArray = null;
        mIOBuffer = null;
        mElement = createElement(rs);
        init(rs, count);
    }

    public  ScriptField_Constants(RenderScript rs, int count, int usages) {
        mItemArray = null;
        mIOBuffer = null;
        mElement = createElement(rs);
        init(rs, count, usages);
    }

    public static ScriptField_Constants create1D(RenderScript rs, int dimX, int usages) {
        ScriptField_Constants obj = new ScriptField_Constants(rs);
        obj.mAllocation = Allocation.createSized(rs, obj.mElement, dimX, usages);
        return obj;
    }

    public static ScriptField_Constants create1D(RenderScript rs, int dimX) {
        return create1D(rs, dimX, Allocation.USAGE_SCRIPT);
    }

    public static ScriptField_Constants create2D(RenderScript rs, int dimX, int dimY) {
        return create2D(rs, dimX, dimY, Allocation.USAGE_SCRIPT);
    }

    public static ScriptField_Constants create2D(RenderScript rs, int dimX, int dimY, int usages) {
        ScriptField_Constants obj = new ScriptField_Constants(rs);
        Type.Builder b = new Type.Builder(rs, obj.mElement);
        b.setX(dimX);
        b.setY(dimY);
        Type t = b.create();
        obj.mAllocation = Allocation.createTyped(rs, t, usages);
        return obj;
    }

    public static Type.Builder createTypeBuilder(RenderScript rs) {
        Element e = createElement(rs);
        return new Type.Builder(rs, e);
    }

    public static ScriptField_Constants createCustom(RenderScript rs, Type.Builder tb, int usages) {
        ScriptField_Constants obj = new ScriptField_Constants(rs);
        Type t = tb.create();
        if (t.getElement() != obj.mElement) {
            throw new RSIllegalArgumentException("Type.Builder did not match expected element type.");
        }
        obj.mAllocation = Allocation.createTyped(rs, t, usages);
        return obj;
    }

    private void copyToArrayLocal(Item i, FieldPacker fp) {
        fp.addF32(i.Drop01);
        fp.addF32(i.Drop02);
        fp.addF32(i.Drop03);
        fp.addF32(i.Drop04);
        fp.addF32(i.Drop05);
        fp.addF32(i.Drop06);
        fp.addF32(i.Drop07);
        fp.addF32(i.Drop08);
        fp.addF32(i.Drop09);
        fp.addF32(i.Drop10);
        fp.addF32(i.Offset);
        fp.addF32(i.Rotate);
        fp.skip(12);
    }

    private void copyToArray(Item i, int index) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        mIOBuffer.reset(index * Item.sizeof);
        copyToArrayLocal(i, mIOBuffer);
    }

    public synchronized void set(Item i, int index, boolean copyNow) {
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        mItemArray[index] = i;
        if (copyNow)  {
            copyToArray(i, index);
            FieldPacker fp = new FieldPacker(Item.sizeof);
            copyToArrayLocal(i, fp);
            mAllocation.setFromFieldPacker(index, fp);
        }

    }

    public synchronized Item get(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index];
    }

    public synchronized void set_Drop01(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Drop01 = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 0, fp);
        }

    }

    public synchronized void set_Drop02(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Drop02 = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 16);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 1, fp);
        }

    }

    public synchronized void set_Drop03(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Drop03 = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 32);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 2, fp);
        }

    }

    public synchronized void set_Drop04(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Drop04 = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 48);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 3, fp);
        }

    }

    public synchronized void set_Drop05(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Drop05 = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 64);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 4, fp);
        }

    }

    public synchronized void set_Drop06(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Drop06 = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 80);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 5, fp);
        }

    }

    public synchronized void set_Drop07(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Drop07 = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 96);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 6, fp);
        }

    }

    public synchronized void set_Drop08(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Drop08 = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 112);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 7, fp);
        }

    }

    public synchronized void set_Drop09(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Drop09 = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 128);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 8, fp);
        }

    }

    public synchronized void set_Drop10(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Drop10 = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 144);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 9, fp);
        }

    }

    public synchronized void set_Offset(int index, Float4 v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Offset = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 160);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(16);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 10, fp);
        }

    }

    public synchronized void set_Rotate(int index, float v, boolean copyNow) {
        if (mIOBuffer == null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
        if (mItemArray == null) mItemArray = new Item[getType().getX() /* count */];
        if (mItemArray[index] == null) mItemArray[index] = new Item();
        mItemArray[index].Rotate = v;
        if (copyNow)  {
            mIOBuffer.reset(index * Item.sizeof + 176);
            mIOBuffer.addF32(v);
            FieldPacker fp = new FieldPacker(4);
            fp.addF32(v);
            mAllocation.setFromFieldPacker(index, 11, fp);
        }

    }

    public synchronized Float4 get_Drop01(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Drop01;
    }

    public synchronized Float4 get_Drop02(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Drop02;
    }

    public synchronized Float4 get_Drop03(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Drop03;
    }

    public synchronized Float4 get_Drop04(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Drop04;
    }

    public synchronized Float4 get_Drop05(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Drop05;
    }

    public synchronized Float4 get_Drop06(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Drop06;
    }

    public synchronized Float4 get_Drop07(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Drop07;
    }

    public synchronized Float4 get_Drop08(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Drop08;
    }

    public synchronized Float4 get_Drop09(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Drop09;
    }

    public synchronized Float4 get_Drop10(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Drop10;
    }

    public synchronized Float4 get_Offset(int index) {
        if (mItemArray == null) return null;
        return mItemArray[index].Offset;
    }

    public synchronized float get_Rotate(int index) {
        if (mItemArray == null) return 0;
        return mItemArray[index].Rotate;
    }

    public synchronized void copyAll() {
        for (int ct = 0; ct < mItemArray.length; ct++) copyToArray(mItemArray[ct], ct);
        mAllocation.setFromFieldPacker(0, mIOBuffer);
    }

    public synchronized void resize(int newSize) {
        if (mItemArray != null)  {
            int oldSize = mItemArray.length;
            int copySize = Math.min(oldSize, newSize);
            if (newSize == oldSize) return;
            Item ni[] = new Item[newSize];
            System.arraycopy(mItemArray, 0, ni, 0, copySize);
            mItemArray = ni;
        }

        mAllocation.resize(newSize);
        if (mIOBuffer != null) mIOBuffer = new FieldPacker(Item.sizeof * getType().getX()/* count */);
    }

}

