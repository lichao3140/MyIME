package com.idata.bluetoothime;

/**
 * Interface to notify the input method when the user clicks a candidate or
 * makes a direction-gesture on candidate view.
 */
/**
 * 候选词视图监听器接口
 * 
 * @ClassName CandidateViewListener
 * @author LiChao
 */
public interface CandidateViewListener {

	/**
	 * 选择了候选词的处理函数
	 * 
	 * @param choiceId
	 */
	public void onClickChoice(int choiceId);

	/**
	 * 向左滑动的手势处理函数
	 */
	public void onToLeftGesture();

	/**
	 * 向右滑动的手势处理函数
	 */
	public void onToRightGesture();

	/**
	 * 向上滑动的手势处理函数
	 */
	public void onToTopGesture();

	/**
	 * 向下滑动的手势处理函数
	 */
	public void onToBottomGesture();
}
