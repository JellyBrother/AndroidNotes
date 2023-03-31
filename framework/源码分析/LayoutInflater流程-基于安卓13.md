# LayoutInflater流程-基于安卓13

## /home/data/aosp/android-13.0.0\_r1/frameworks/base/core/java/android/view/LayoutInflater.java

## View view = LayoutInflater.from(context).inflate(remoteViews.getLayoutId(), null);

    public static LayoutInflater from(@UiContext Context context) {
        LayoutInflater LayoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (LayoutInflater == null) {
            throw new AssertionError("LayoutInflater not found.");
        }
        return LayoutInflater;
    }

我们可以继承Context，复写getSystemService方法，所以分析的时候，这里的上下文很关键

    @Override
    public Object getSystemService(String name) {
        if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mLayoutInflater == null) {
                LayoutInflater layoutInflater = (LayoutInflater) super.getSystemService(name);
                mLayoutInflater = layoutInflater.cloneInContext(this);
            }
            return mLayoutInflater;
        }
        return super.getSystemService(name);
    }

## inflate方法内调用tryInflatePrecompiled获取最外层控件，调用inflate给控件添加属性：

    public View inflate(@LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        final Resources res = getContext().getResources();
        if (DEBUG) {
            Log.d(TAG, "INFLATING from resource: \"" + res.getResourceName(resource) + "\" ("
                  + Integer.toHexString(resource) + ")");
        }
    
        View view = tryInflatePrecompiled(resource, res, root, attachToRoot);
        if (view != null) {
            return view;
        }
        XmlResourceParser parser = res.getLayout(resource);
        try {
            return inflate(parser, root, attachToRoot);
        } finally {
            parser.close();
        }
    }

## tryInflatePrecompiled方法会创建最外层控件，并且设置主题和LayoutParams

    private View tryInflatePrecompiled(@LayoutRes int resource, Resources res, @Nullable ViewGroup root,
        boolean attachToRoot) {
        if (!mUseCompiledView) {
            return null;
        }
        Trace.traceBegin(Trace.TRACE_TAG_VIEW, "inflate (precompiled)");
        // Try to inflate using a precompiled layout.
        String pkg = res.getResourcePackageName(resource);
        String layout = res.getResourceEntryName(resource);
        try {
            Class clazz = Class.forName("" + pkg + ".CompiledView", false, mPrecompiledClassLoader);
            Method inflater = clazz.getMethod(layout, Context.class, int.class);
            View view = (View) inflater.invoke(null, mContext, resource);
    
            if (view != null && root != null) {
                // We were able to use the precompiled inflater, but now we need to do some work to
                // attach the view to the root correctly.
                XmlResourceParser parser = res.getLayout(resource);
                try {
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    advanceToRootNode(parser);
                    ViewGroup.LayoutParams params = root.generateLayoutParams(attrs);
    
                    if (attachToRoot) {
                        root.addView(view, params);
                    } else {
                        view.setLayoutParams(params);
                    }
                } finally {
                    parser.close();
                }
            }
        return null;
    }

/home/data/aosp/android-13.0.0\_r1/frameworks/base/core/java/android/view/ViewGroup.java

## generateLayoutParams方法根据外层attrs模板，得到LayoutParams，在LayoutParams的构造方法中，根Context的主题相关，我们可以重写Context的getTheme方法来构建主题，要根Resources进行关联

    public LayoutParams(Context c, AttributeSet attrs) {
        TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ViewGroup_Layout);
        setBaseAttributes(a,
                R.styleable.ViewGroup_Layout_layout_width,
                R.styleable.ViewGroup_Layout_layout_height);
        a.recycle();
    }

    public final TypedArray obtainStyledAttributes(
            @Nullable AttributeSet set, @NonNull @StyleableRes int[] attrs) {
        return getTheme().obtainStyledAttributes(set, attrs, 0, 0);
    }

/home/data/aosp/android-13.0.0\_r1/frameworks/base/core/java/android/content/res/TypedArray.java

从mData数组里面获取值，注意这里的index范围是 >=16并且<=31  或者=5  或者=2，不然会报错找不到

    public int getLayoutDimension(@StyleableRes int index, String name) {
        if (mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
    
        final int attrIndex = index;
        index *= STYLE_NUM_ENTRIES;
    
        final int[] data = mData;
        final int type = data[index + STYLE_TYPE];
        if (type >= TypedValue.TYPE_FIRST_INT
                && type <= TypedValue.TYPE_LAST_INT) {
            return data[index + STYLE_DATA];
        } else if (type == TypedValue.TYPE_DIMENSION) {
            return TypedValue.complexToDimensionPixelSize(data[index + STYLE_DATA], mMetrics);
        } else if (type == TypedValue.TYPE_ATTRIBUTE) {
            final TypedValue value = mValue;
            getValueAt(index, value);
            throw new UnsupportedOperationException(
                    "Failed to resolve attribute at index " + attrIndex + ": " + value
                            + ", theme=" + mTheme);
        }
    
        throw new UnsupportedOperationException(getPositionDescription()
                + ": You must supply a " + name + " attribute." + ", theme=" + mTheme);
    }

## 最外层的控件准备好后，开始循环解析子元素，并添加到上层节点

    public View inflate(XmlPullParser parser, @Nullable ViewGroup root, boolean attachToRoot) {
        synchronized (mConstructorArgs) {
            final Context inflaterContext = mContext;
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            Context lastContext = (Context) mConstructorArgs[0];
            mConstructorArgs[0] = inflaterContext;
            View result = root;
            try {
                advanceToRootNode(parser);
                final String name = parser.getName();
                if (TAG_MERGE.equals(name)) {
                    if (root == null || !attachToRoot) {
                        throw new InflateException("<merge /> can be used only with a valid "
                                + "ViewGroup root and attachToRoot=true");
                    }
                    rInflate(parser, root, inflaterContext, attrs, false);
                } else {
                    // Temp is the root view that was found in the xml
                    final View temp = createViewFromTag(root, name, inflaterContext, attrs);
                    ViewGroup.LayoutParams params = null;
                    if (root != null) {
                        // Create layout params that match root, if supplied
                        params = root.generateLayoutParams(attrs);
                        if (!attachToRoot) {
                            // Set the layout params for temp if we are not
                            // attaching. (If we are, we use addView, below)
                            temp.setLayoutParams(params);
                        }
                    }
                    // Inflate all children under temp against its context.
                    rInflateChildren(parser, temp, attrs, true);
                    // We are supposed to attach all the views we found (int temp)
                    // to root. Do that now.
                    if (root != null && attachToRoot) {
                        root.addView(temp, params);
                    }
                    // Decide whether to return the root that was passed in or the
                    // top view found in xml.
                    if (root == null || !attachToRoot) {
                        result = temp;
                    }
                }
            return result;
        }
    }

先判断是不是merge节点，是就rInflate(parser, root, inflaterContext, attrs, false);

否则

    View createViewFromTag(View parent, String name, Context context, AttributeSet attrs,
            boolean ignoreThemeAttr) {
        if (name.equals("view")) {
            name = attrs.getAttributeValue(null, "class");
        }
    
        // Apply a theme wrapper, if allowed and one is specified.
        if (!ignoreThemeAttr) {
            final TypedArray ta = context.obtainStyledAttributes(attrs, ATTRS_THEME);
            final int themeResId = ta.getResourceId(0, 0);
            if (themeResId != 0) {
                context = new ContextThemeWrapper(context, themeResId);
            }
            ta.recycle();
        }
    
        try {
            View view = tryCreateView(parent, name, context, attrs);
    
            if (view == null) {
                final Object lastContext = mConstructorArgs[0];
                mConstructorArgs[0] = context;
                try {
                    if (-1 == name.indexOf('.')) {
                        view = onCreateView(context, parent, name, attrs);
                    } else {
                        view = createView(context, name, null, attrs);
                    }
                } finally {
                    mConstructorArgs[0] = lastContext;
                }
            }
    
            return view;
    }

先tryCreateView工厂类创建mFactory2.onCreateView，再判断.走自定义控件创建onCreateView，然后是原生控件创建createView

    public final View tryCreateView(@Nullable View parent, @NonNull String name,
        @NonNull Context context,
        @NonNull AttributeSet attrs) {
        if (name.equals(TAG_1995)) {
            // Let's party like it's 1995!
            return new BlinkLayout(context, attrs);
        }
    
        View view;
        if (mFactory2 != null) {
            view = mFactory2.onCreateView(parent, name, context, attrs);
        } else if (mFactory != null) {
            view = mFactory.onCreateView(name, context, attrs);
        } else {
            view = null;
        }
    
        if (view == null && mPrivateFactory != null) {
            view = mPrivateFactory.onCreateView(parent, name, context, attrs);
        }
    
        return view;
    }

mFactory2.onCreateView基本是Activity创建

## 自定义创建走createViewFromTag，遍历循环后走的还是onCreateView和createView

    View createViewFromTag(View parent, String name, Context context, AttributeSet attrs,
            // Apply a theme wrapper, if allowed and one is specified.
            if (!ignoreThemeAttr) {
                final TypedArray ta = context.obtainStyledAttributes(attrs, ATTRS_THEME);
                final int themeResId = ta.getResourceId(0, 0);
                if (themeResId != 0) {
                    context = new ContextThemeWrapper(context, themeResId);
                }
                ta.recycle();
            }
            try {
                View view = tryCreateView(parent, name, context, attrs);
    
                if (view == null) {
                    final Object lastContext = mConstructorArgs[0];
                    mConstructorArgs[0] = context;
                    try {
                        if (-1 == name.indexOf('.')) {
                            view = onCreateView(context, parent, name, attrs);
                        } else {
                            view = createView(context, name, null, attrs);
                        }
                    } finally {
                        mConstructorArgs[0] = lastContext;
                    }
                }
                return view;
        }

    public final View createView(@NonNull Context viewContext, @NonNull String name,
            @Nullable String prefix, @Nullable AttributeSet attrs)
            throws ClassNotFoundException, InflateException {
        Class<? extends View> clazz = null;
        try {
            if (constructor == null) {
                // Class not found in the cache, see if it's real, and try to add it
                clazz = Class.forName(prefix != null ? (prefix + name) : name, false,
                        mContext.getClassLoader()).asSubclass(View.class);
    
                if (mFilter != null && clazz != null) {
                    boolean allowed = mFilter.onLoadClass(clazz);
                    if (!allowed) {
                        failNotAllowed(name, prefix, viewContext, attrs);
                    }
                }
                constructor = clazz.getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                sConstructorMap.put(name, constructor);
            } else {
                // If we have a filter, apply it to cached constructor
                if (mFilter != null) {
                    // Have we seen this name before?
                    Boolean allowedState = mFilterMap.get(name);
                    if (allowedState == null) {
                        // New class -- remember whether it is allowed
                        clazz = Class.forName(prefix != null ? (prefix + name) : name, false,
                                mContext.getClassLoader()).asSubclass(View.class);
    
                        boolean allowed = clazz != null && mFilter.onLoadClass(clazz);
                        mFilterMap.put(name, allowed);
                        if (!allowed) {
                            failNotAllowed(name, prefix, viewContext, attrs);
                        }
                    } else if (allowedState.equals(Boolean.FALSE)) {
                        failNotAllowed(name, prefix, viewContext, attrs);
                    }
                }
            }
            Object lastContext = mConstructorArgs[0];
            mConstructorArgs[0] = viewContext;
            Object[] args = mConstructorArgs;
            args[1] = attrs;
            try {
                final View view = constructor.newInstance(args);
                if (view instanceof ViewStub) {
                    // Use the same context when inflating ViewStub later.
                    final ViewStub viewStub = (ViewStub) view;
                    viewStub.setLayoutInflater(cloneInContext((Context) args[0]));
                }
                return view;
    }

## tryCreateView工厂类创建mFactory2.onCreateView，Factory和Factory2都是LayoutInflater的内部接口

AppCompatActivity继承FragmentActivity继承Activity实现Factory2

AppCompatActivity的构造方法会创建AppCompatDelegate的子类AppCompatDelegateImpl的installViewFactory绑定工厂

LayoutInflaterCompat.setFactory2(layoutInflater, this);

Fragment调用onGetLayoutInflater调用getLayoutInflater绑定工厂

LayoutInflaterCompat.setFactory2(result, mChildFragmentManager.getLayoutInflaterFactory());

LayoutInflaterCompat.setFactory2方法调用inflater.setFactory2(factory);进行绑定

在LayoutInflater的tryCreateView方法会调用Factory2的onCreateView方法，调用绑定对象的

onCreateView方法

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/5yBRq1o2yYQQldv1/img/e011b0c5-711c-4bfe-b62b-debcb8ffbae1.png)

AppCompatDelegateImpl本身也是Factory2的实现类，又跟Activity绑定，因而调用AppCompatDelegateImpl的onCreateView方法，AppCompatDelegateImpl的onCreateView方法调用了androidx.appcompat.app.AppCompatViewInflater#createView(android.view.View, java.lang.String, android.content.Context, android.util.AttributeSet, boolean, boolean, boolean, boolean)，从而工厂类创建出了各个控件

    final View createView(View parent, final String name, @NonNull Context context,
            @NonNull AttributeSet attrs, boolean inheritContext,
            boolean readAndroidTheme, boolean readAppTheme, boolean wrapContext) {
        final Context originalContext = context;
    
        // We can emulate Lollipop's android:theme attribute propagating down the view hierarchy
        // by using the parent's context
        if (inheritContext && parent != null) {
            context = parent.getContext();
        }
        if (readAndroidTheme || readAppTheme) {
            // We then apply the theme on the context, if specified
            context = themifyContext(context, attrs, readAndroidTheme, readAppTheme);
        }
        if (wrapContext) {
            context = TintContextWrapper.wrap(context);
        }
    
        View view = null;
    
        // We need to 'inject' our tint aware Views in place of the standard framework versions
        switch (name) {
            case "TextView":
                view = createTextView(context, attrs);
                verifyNotNull(view, name);
                break;
            case "ImageView":
                view = createImageView(context, attrs);
                verifyNotNull(view, name);
                break;
            case "Button":
                view = createButton(context, attrs);
                verifyNotNull(view, name);
                break;
            case "EditText":
                view = createEditText(context, attrs);
                verifyNotNull(view, name);
                break;
            case "Spinner":
                view = createSpinner(context, attrs);
                verifyNotNull(view, name);
                break;
            case "ImageButton":
                view = createImageButton(context, attrs);
                verifyNotNull(view, name);
                break;
            case "CheckBox":
                view = createCheckBox(context, attrs);
                verifyNotNull(view, name);
                break;
            case "RadioButton":
                view = createRadioButton(context, attrs);
                verifyNotNull(view, name);
                break;
            case "CheckedTextView":
                view = createCheckedTextView(context, attrs);
                verifyNotNull(view, name);
                break;
            case "AutoCompleteTextView":
                view = createAutoCompleteTextView(context, attrs);
                verifyNotNull(view, name);
                break;
            case "MultiAutoCompleteTextView":
                view = createMultiAutoCompleteTextView(context, attrs);
                verifyNotNull(view, name);
                break;
            case "RatingBar":
                view = createRatingBar(context, attrs);
                verifyNotNull(view, name);
                break;
            case "SeekBar":
                view = createSeekBar(context, attrs);
                verifyNotNull(view, name);
                break;
            case "ToggleButton":
                view = createToggleButton(context, attrs);
                verifyNotNull(view, name);
                break;
            default:
                // The fallback that allows extending class to take over view inflation
                // for other tags. Note that we don't check that the result is not-null.
                // That allows the custom inflater path to fall back on the default one
                // later in this method.
                view = createView(context, name, attrs);
        }
    
        if (view == null && originalContext != context) {
            // If the original context does not equal our themed context, then we need to manually
            // inflate it using the name so that android:theme takes effect.
            view = createViewFromTag(context, name, attrs);
        }
    
        if (view != null) {
            // If we have created a view, check its android:onClick
            checkOnClickListener(view, attrs);
            backportAccessibilityAttributes(context, view, attrs);
        }
    
        return view;
    }