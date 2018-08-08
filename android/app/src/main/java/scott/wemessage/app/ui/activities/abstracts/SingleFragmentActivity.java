/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package scott.wemessage.app.ui.activities.abstracts;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public abstract class SingleFragmentActivity extends BaseActivity {

    private @LayoutRes int layoutId;
    private int containerId;

    public SingleFragmentActivity(@LayoutRes int layoutId, int containerId){
        this.layoutId = layoutId;
        this.containerId = containerId;
    }

    public abstract Fragment createFragment();

    @LayoutRes
    public int getLayoutResId(){
        return layoutId;
    }

    public int getContainerId(){
        return containerId;
    }

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(getLayoutResId());

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(getContainerId());

        if (fragment == null){
            fragment = createFragment();
            fragmentManager.beginTransaction().add(getContainerId(), fragment).commit();
        }
    }
}
