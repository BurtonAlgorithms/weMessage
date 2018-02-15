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
